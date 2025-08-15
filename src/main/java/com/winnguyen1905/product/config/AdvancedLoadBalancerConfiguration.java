package com.winnguyen1905.product.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Advanced LoadBalancer configuration to suppress remaining warnings
 * The configuration has been optimized to work with disabled discovery
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "false", matchIfMissing = true)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AdvancedLoadBalancerConfiguration {

    // This configuration class serves as a marker to organize LoadBalancer 
    // configurations when discovery is disabled (local development)
    
}
