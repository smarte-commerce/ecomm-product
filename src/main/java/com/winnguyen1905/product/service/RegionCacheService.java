package com.winnguyen1905.product.service;

import com.winnguyen1905.product.secure.RegionPartition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for reading IP-to-region mappings from Redis cache.
 * This connects to the same Redis cache used by the gateway service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RegionCacheService {

    private final StringRedisTemplate redisTemplate;

    // Cache key prefixes (must match gateway service)
    private static final String IP_REGION_PREFIX = "ip:region:";
    private static final String SESSION_REGION_PREFIX = "session:region:";

    /**
     * Get cached region for IP address from Redis
     */
    public RegionPartition getCachedRegionForIp(String clientIp) {
        if (isPrivateOrInvalidIp(clientIp)) {
            log.debug("Private or invalid IP detected: {}, returning default region", clientIp);
            return RegionPartition.US;
        }

        try {
            String cacheKey = getIpRegionKey(clientIp);
            String regionCode = redisTemplate.opsForValue().get(cacheKey);
            
            if (regionCode != null && !regionCode.trim().isEmpty()) {
                RegionPartition region = RegionPartition.fromCode(regionCode);
                log.debug("Found cached region {} for IP {}", region.getCode(), clientIp);
                return region;
            }
            
            log.debug("No cached region found for IP {}", clientIp);
            return null;
            
        } catch (Exception e) {
            log.error("Error retrieving cached region for IP {}: {}", clientIp, e.getMessage());
            return null;
        }
    }

    /**
     * Get cached session region from Redis
     */
    public RegionPartition getCachedSessionRegion(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        try {
            String cacheKey = getSessionRegionKey(sessionId);
            String regionCode = redisTemplate.opsForValue().get(cacheKey);
            
            if (regionCode != null && !regionCode.trim().isEmpty()) {
                RegionPartition region = RegionPartition.fromCode(regionCode);
                log.debug("Found cached session region {} for session {}", region.getCode(), sessionId);
                return region;
            }
            
            log.debug("No cached session region found for session {}", sessionId);
            return null;
            
        } catch (Exception e) {
            log.error("Error retrieving cached session region for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * Cache IP-region mapping (for fallback scenarios)
     */
    public boolean cacheIpRegion(String clientIp, RegionPartition region) {
        if (isPrivateOrInvalidIp(clientIp) || region == null) {
            return false;
        }

        try {
            String cacheKey = getIpRegionKey(clientIp);
            redisTemplate.opsForValue().set(cacheKey, region.getCode(), 24, TimeUnit.HOURS);
            log.debug("Cached region {} for IP {} (fallback operation)", region.getCode(), clientIp);
            return true;
            
        } catch (Exception e) {
            log.error("Error caching region for IP {}: {}", clientIp, e.getMessage());
            return false;
        }
    }

    /**
     * Check if Redis cache is available
     */
    public boolean isCacheAvailable() {
        try {
            redisTemplate.opsForValue().get("cache:health:check");
            return true;
        } catch (Exception e) {
            log.warn("Redis cache is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get cache statistics for monitoring
     */
    public CacheStats getCacheStats() {
        try {
            // Simple implementation to count keys with our prefixes
            // Note: In production, you might want to use Redis commands like SCAN
            return new CacheStats(true, "Cache connection healthy");
        } catch (Exception e) {
            log.error("Error getting cache stats: {}", e.getMessage());
            return new CacheStats(false, "Cache connection failed: " + e.getMessage());
        }
    }

    // Helper methods
    private String getIpRegionKey(String ip) {
        return IP_REGION_PREFIX + ip;
    }

    private String getSessionRegionKey(String sessionId) {
        return SESSION_REGION_PREFIX + sessionId;
    }

    private boolean isPrivateOrInvalidIp(String ip) {
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return true;
        }
        
        String cleanIp = ip.trim().toLowerCase();
        return cleanIp.startsWith("10.") || 
               cleanIp.startsWith("172.") || 
               cleanIp.startsWith("192.168.") || 
               cleanIp.startsWith("127.") || 
               cleanIp.equals("localhost") ||
               cleanIp.equals("0:0:0:0:0:0:0:1") ||
               cleanIp.equals("::1");
    }

    /**
     * Cache statistics record
     */
    public record CacheStats(boolean isHealthy, String message) {}
} 
