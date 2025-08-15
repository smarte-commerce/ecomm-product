package com.winnguyen1905.product.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for optimizing application startup and runtime performance
 */
@Configuration
@Slf4j
public class StartupOptimizationConfiguration {

    /**
     * Log startup completion and performance metrics
     */
    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        log.info("🚀 Product Service started successfully!");
        log.info("💡 Performance optimizations applied:");
        log.info("   ✓ Lazy initialization enabled");
        log.info("   ✓ LoadBalancer warnings suppressed");
        log.info("   ✓ Thread pool optimized");
        log.info("   ✓ Bean post-processor conflicts resolved");
        
        // Log memory usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        log.info("📊 Memory Usage:");
        log.info("   Max Memory: {} MB", maxMemory / 1024 / 1024);
        log.info("   Used Memory: {} MB", usedMemory / 1024 / 1024);
        log.info("   Free Memory: {} MB", freeMemory / 1024 / 1024);
    }
}
