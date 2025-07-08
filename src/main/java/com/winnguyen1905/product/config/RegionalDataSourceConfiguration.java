package com.winnguyen1905.product.config;

import com.winnguyen1905.product.secure.RegionPartition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for regional database routing.
 * Automatically routes database queries to the appropriate regional cluster
 * based on the user's detected region.
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class RegionalDataSourceConfiguration {

    /**
     * Primary routing datasource that determines which regional database to use
     */
    @Primary
    @Bean
    public DataSource dataSource() {
        RegionalRoutingDataSource routingDataSource = new RegionalRoutingDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(RegionPartition.US, usDataSource());
        targetDataSources.put(RegionPartition.EU, euDataSource());
        targetDataSources.put(RegionPartition.ASIA, asiaDataSource());
        
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(usDataSource()); // Default to US
        
        return routingDataSource;
    }

    /**
     * US region database configuration
     */
    @Bean
    @ConfigurationProperties("app.datasource.us")
    public DataSource usDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://cockroachdb-1:26257/ecommerce_us?sslmode=disable")
                .username("root")
                .password("")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    /**
     * EU region database configuration
     */
    @Bean
    @ConfigurationProperties("app.datasource.eu")
    public DataSource euDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://cockroachdb-2:26257/ecommerce_eu?sslmode=disable")
                .username("root")
                .password("")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    /**
     * ASIA region database configuration
     */
    @Bean
    @ConfigurationProperties("app.datasource.asia")
    public DataSource asiaDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://cockroachdb-3:26257/ecommerce_asia?sslmode=disable")
                .username("root")
                .password("")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    /**
     * Custom routing datasource that determines the target database
     * based on the current user's region
     */
    public static class RegionalRoutingDataSource extends AbstractRoutingDataSource {

        @Override
        protected Object determineCurrentLookupKey() {
            RegionPartition region = getCurrentUserRegion();
            log.debug("Routing database query to region: {}", region);
            return region;
        }

        /**
         * Determine current user region from various sources
         */
        private RegionPartition getCurrentUserRegion() {
            try {
                // Try to get region from request attributes (set by gateway)
                ServletRequestAttributes requestAttributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (requestAttributes != null) {
                    // Check for region header from gateway
                    String regionCode = requestAttributes.getRequest().getHeader("X-Region-Code");
                    if (regionCode != null && !regionCode.trim().isEmpty()) {
                        try {
                            return RegionPartition.fromCode(regionCode);
                        } catch (Exception e) {
                            log.warn("Invalid region code from header: {}", regionCode);
                        }
                    }
                    
                    // Check for region attribute set by argument resolver
                    Object regionAttr = requestAttributes.getRequest().getAttribute("user.region");
                    if (regionAttr instanceof RegionPartition) {
                        return (RegionPartition) regionAttr;
                    }
                    
                    // Try to extract from client IP if available
                    String clientIp = requestAttributes.getRequest().getHeader("X-Client-IP");
                    if (clientIp != null && !clientIp.trim().isEmpty() && !"unknown".equals(clientIp)) {
                        // Simple IP-based region detection fallback
                        return determineRegionFromIp(clientIp);
                    }
                }
                
                // Check thread-local region context
                RegionPartition threadLocalRegion = RegionalContext.getCurrentRegion();
                if (threadLocalRegion != null) {
                    return threadLocalRegion;
                }
                
            } catch (Exception e) {
                log.error("Error determining current user region: {}", e.getMessage());
            }
            
            // Default fallback
            log.debug("No region detected, defaulting to US");
            return RegionPartition.US;
        }

        /**
         * Simple IP-based region detection as fallback
         */
        private RegionPartition determineRegionFromIp(String ip) {
            // This is a simplified version - in production you'd use a proper GeoIP service
            if (ip.startsWith("192.168") || ip.startsWith("10.") || ip.startsWith("172.")) {
                return RegionPartition.US; // Local IPs default to US
            }
            
            // Add more sophisticated IP-based detection here
            // For now, just return US as default
            return RegionPartition.US;
        }
    }

    /**
     * Thread-local context for storing current user's region
     * Used as fallback when request context is not available
     */
    public static class RegionalContext {
        private static final ThreadLocal<RegionPartition> CURRENT_REGION = new ThreadLocal<>();

        public static void setCurrentRegion(RegionPartition region) {
            CURRENT_REGION.set(region);
        }

        public static RegionPartition getCurrentRegion() {
            return CURRENT_REGION.get();
        }

        public static void clear() {
            CURRENT_REGION.remove();
        }
    }
} 
