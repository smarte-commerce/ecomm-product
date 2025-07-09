package com.winnguyen1905.product.config;

import com.winnguyen1905.product.secure.RegionPartition;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for regional fallback mechanisms.
 * Provides intelligent fallback when regional services are unavailable,
 * including cross-region data replication and circuit breaker patterns.
 */
@Slf4j
@Configuration
@EnableAsync
public class RegionalFallbackConfiguration {

    /**
     * Circuit breaker registry for regional services
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate threshold
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before retry
                .slidingWindowSize(10) // Consider last 10 calls
                .minimumNumberOfCalls(5) // Minimum calls before opening circuit
                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open state
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        
        // Create circuit breakers for each regional service
        for (RegionPartition region : RegionPartition.values()) {
            String cbName = "database-" + region.getCode();
            CircuitBreaker circuitBreaker = registry.circuitBreaker(cbName);
            
            circuitBreaker.getEventPublisher().onStateTransition(event -> 
                log.warn("Circuit breaker {} transitioned from {} to {}", 
                        cbName, event.getStateTransition().getFromState(), 
                        event.getStateTransition().getToState()));
        }
        
        return registry;
    }

    /**
     * Regional fallback service that manages cross-region data access
     */
    @Bean
    public RegionalFallbackService regionalFallbackService(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RedisTemplate<String, Object> redisTemplate) {
        return new RegionalFallbackService(circuitBreakerRegistry, redisTemplate);
    }

    /**
     * Regional health monitor that tracks the health of regional services
     */
    @Bean
    public RegionalHealthMonitor regionalHealthMonitor(
            CircuitBreakerRegistry circuitBreakerRegistry) {
        return new RegionalHealthMonitor(circuitBreakerRegistry);
    }

    /**
     * Service that handles fallback logic when regional services are unavailable
     */
    @Service
    @Slf4j
    public static class RegionalFallbackService {
        
        private final CircuitBreakerRegistry circuitBreakerRegistry;
        private final RedisTemplate<String, Object> redisTemplate;
        private final Map<RegionPartition, RegionPartition> fallbackMapping;
        private final Map<String, Object> crossRegionCache = new ConcurrentHashMap<>();
        
        public RegionalFallbackService(CircuitBreakerRegistry circuitBreakerRegistry,
                                     RedisTemplate<String, Object> redisTemplate) {
            this.circuitBreakerRegistry = circuitBreakerRegistry;
            this.redisTemplate = redisTemplate;
            this.fallbackMapping = initializeFallbackMapping();
        }

        /**
         * Execute operation with regional fallback
         */
        public <T> T executeWithFallback(RegionPartition primaryRegion, 
                                        String operationName,
                                        java.util.function.Supplier<T> operation) {
            
            CircuitBreaker primaryCircuitBreaker = circuitBreakerRegistry
                    .circuitBreaker("database-" + primaryRegion.getCode());
            
            // Try primary region first
            try {
                return primaryCircuitBreaker.executeSupplier(() -> {
                    log.debug("Executing {} in primary region: {}", operationName, primaryRegion);
                    RegionalDataSourceConfiguration.RegionalContext.setCurrentRegion(primaryRegion);
                    return operation.get();
                });
            } catch (Exception e) {
                log.warn("Primary region {} failed for operation {}: {}", 
                        primaryRegion, operationName, e.getMessage());
                
                // Try fallback region
                return executeWithFallbackRegion(primaryRegion, operationName, operation);
            }
        }

        /**
         * Execute operation in fallback region
         */
        private <T> T executeWithFallbackRegion(RegionPartition primaryRegion,
                                               String operationName,
                                               java.util.function.Supplier<T> operation) {
            
            RegionPartition fallbackRegion = getFallbackRegion(primaryRegion);
            
            if (fallbackRegion == null) {
                throw new RuntimeException("No fallback region available for " + primaryRegion);
            }
            
            CircuitBreaker fallbackCircuitBreaker = circuitBreakerRegistry
                    .circuitBreaker("database-" + fallbackRegion.getCode());
            
            try {
                log.info("Executing {} in fallback region: {} (primary {} unavailable)", 
                        operationName, fallbackRegion, primaryRegion);
                
                return fallbackCircuitBreaker.executeSupplier(() -> {
                    RegionalDataSourceConfiguration.RegionalContext.setCurrentRegion(fallbackRegion);
                    
                    // Check cross-region cache first
                    String cacheKey = generateCrossRegionCacheKey(primaryRegion, operationName);
                    T cachedResult = getCachedResult(cacheKey);
                    if (cachedResult != null) {
                        log.debug("Returning cached result for {} from fallback region", operationName);
                        return cachedResult;
                    }
                    
                    // Execute in fallback region
                    T result = operation.get();
                    
                    // Cache result for cross-region access
                    cacheResultForCrossRegion(cacheKey, result);
                    
                    // Async replication back to primary when it recovers
                    scheduleReplicationToPrimary(primaryRegion, operationName, result);
                    
                    return result;
                });
                
            } catch (Exception fallbackException) {
                log.error("Fallback region {} also failed for operation {}: {}", 
                         fallbackRegion, operationName, fallbackException.getMessage());
                
                // Try cached data as last resort
                String cacheKey = generateCrossRegionCacheKey(primaryRegion, operationName);
                T cachedResult = getCachedResult(cacheKey);
                if (cachedResult != null) {
                    log.warn("Returning stale cached data for {} due to regional failures", operationName);
                    return cachedResult;
                }
                
                throw new RuntimeException("All regional services unavailable for " + operationName, 
                                         fallbackException);
            }
        }

        /**
         * Get fallback region for a primary region
         */
        public RegionPartition getFallbackRegion(RegionPartition primaryRegion) {
            return fallbackMapping.get(primaryRegion);
        }

        /**
         * Check if a region is currently available
         */
        public boolean isRegionAvailable(RegionPartition region) {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry
                    .circuitBreaker("database-" + region.getCode());
            return circuitBreaker.getState() == CircuitBreaker.State.CLOSED;
        }

        /**
         * Get list of available regions ordered by preference
         */
        public List<RegionPartition> getAvailableRegionsInOrder(RegionPartition preferredRegion) {
            List<RegionPartition> availableRegions = new ArrayList<>();
            
            // Add preferred region first if available
            if (isRegionAvailable(preferredRegion)) {
                availableRegions.add(preferredRegion);
            }
            
            // Add fallback region if available
            RegionPartition fallbackRegion = getFallbackRegion(preferredRegion);
            if (fallbackRegion != null && isRegionAvailable(fallbackRegion)) {
                availableRegions.add(fallbackRegion);
            }
            
            // Add other available regions
            for (RegionPartition region : RegionPartition.values()) {
                if (!availableRegions.contains(region) && isRegionAvailable(region)) {
                    availableRegions.add(region);
                }
            }
            
            return availableRegions;
        }

        @Async
        public void scheduleReplicationToPrimary(RegionPartition primaryRegion, 
                                               String operationName, 
                                               Object data) {
            CompletableFuture.delayedExecutor(Duration.ofMinutes(5).toNanos(), 
                                            java.util.concurrent.TimeUnit.NANOSECONDS)
                    .execute(() -> {
                        if (isRegionAvailable(primaryRegion)) {
                            log.info("Attempting to replicate {} back to primary region {}", 
                                   operationName, primaryRegion);
                            // Implementation would depend on specific replication strategy
                        }
                    });
        }

        private Map<RegionPartition, RegionPartition> initializeFallbackMapping() {
            Map<RegionPartition, RegionPartition> mapping = new HashMap<>();
            
            // Define fallback relationships based on geographical proximity and latency
            mapping.put(RegionPartition.US, RegionPartition.EU);     // US -> EU
            mapping.put(RegionPartition.EU, RegionPartition.ASIA);   // EU -> ASIA
            mapping.put(RegionPartition.ASIA, RegionPartition.US);   // ASIA -> US
            
            return mapping;
        }

        private String generateCrossRegionCacheKey(RegionPartition region, String operationName) {
            return "cross-region:" + region.getCode() + ":" + operationName;
        }

        @SuppressWarnings("unchecked")
        private <T> T getCachedResult(String cacheKey) {
            try {
                return (T) redisTemplate.opsForValue().get(cacheKey);
            } catch (Exception e) {
                log.debug("Error retrieving cached result for {}: {}", cacheKey, e.getMessage());
                return null;
            }
        }

        private void cacheResultForCrossRegion(String cacheKey, Object result) {
            try {
                // Cache for 1 hour with cross-region prefix
                redisTemplate.opsForValue().set(cacheKey, result, Duration.ofHours(1));
                log.debug("Cached result for cross-region access: {}", cacheKey);
            } catch (Exception e) {
                log.warn("Error caching result for cross-region access: {}", e.getMessage());
            }
        }
    }

    /**
     * Service that monitors the health of regional services
     */
    @Service
    @Slf4j
    public static class RegionalHealthMonitor {
        
        private final CircuitBreakerRegistry circuitBreakerRegistry;
        private final Map<RegionPartition, RegionalHealthStatus> healthStatus = new ConcurrentHashMap<>();
        
        public RegionalHealthMonitor(CircuitBreakerRegistry circuitBreakerRegistry) {
            this.circuitBreakerRegistry = circuitBreakerRegistry;
            initializeHealthStatus();
        }

        /**
         * Get current health status for all regions
         */
        public Map<RegionPartition, RegionalHealthStatus> getHealthStatus() {
            updateHealthStatus();
            return new HashMap<>(healthStatus);
        }

        /**
         * Get health status for specific region
         */
        public RegionalHealthStatus getRegionHealth(RegionPartition region) {
            updateHealthStatus();
            return healthStatus.getOrDefault(region, 
                    new RegionalHealthStatus(region, false, "Unknown", null));
        }

        /**
         * Check if region is healthy
         */
        public boolean isRegionHealthy(RegionPartition region) {
            return getRegionHealth(region).isHealthy();
        }

        /**
         * Get current user's region from request context
         */
        public RegionPartition getCurrentUserRegion() {
            try {
                // Try thread-local context first
                RegionPartition region = RegionalDataSourceConfiguration.RegionalContext.getCurrentRegion();
                if (region != null) {
                    return region;
                }
                
                // Try request headers
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    String regionCode = attributes.getRequest().getHeader("X-Region-Code");
                    if (regionCode != null && !regionCode.trim().isEmpty()) {
                        return RegionPartition.fromCode(regionCode);
                    }
                }
            } catch (Exception e) {
                log.debug("Error determining current user region: {}", e.getMessage());
            }
            
            return RegionPartition.US; // Default fallback
        }

        private void initializeHealthStatus() {
            for (RegionPartition region : RegionPartition.values()) {
                healthStatus.put(region, new RegionalHealthStatus(region, true, "Initializing", null));
            }
        }

        private void updateHealthStatus() {
            for (RegionPartition region : RegionPartition.values()) {
                CircuitBreaker circuitBreaker = circuitBreakerRegistry
                        .circuitBreaker("database-" + region.getCode());
                
                boolean isHealthy = circuitBreaker.getState() == CircuitBreaker.State.CLOSED;
                String status = circuitBreaker.getState().toString();
                
                RegionalHealthStatus currentStatus = new RegionalHealthStatus(
                        region, isHealthy, status, new Date());
                
                healthStatus.put(region, currentStatus);
            }
        }
    }

    /**
     * Represents the health status of a regional service
     */
    public static class RegionalHealthStatus {
        private final RegionPartition region;
        private final boolean healthy;
        private final String status;
        private final Date lastChecked;

        public RegionalHealthStatus(RegionPartition region, boolean healthy, String status, Date lastChecked) {
            this.region = region;
            this.healthy = healthy;
            this.status = status;
            this.lastChecked = lastChecked;
        }

        public RegionPartition getRegion() { return region; }
        public boolean isHealthy() { return healthy; }
        public String getStatus() { return status; }
        public Date getLastChecked() { return lastChecked; }

        @Override
        public String toString() {
            return String.format("RegionalHealthStatus{region=%s, healthy=%s, status='%s', lastChecked=%s}",
                    region, healthy, status, lastChecked);
        }
    }
} 
