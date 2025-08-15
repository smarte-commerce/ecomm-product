package com.winnguyen1905.product.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * LoadBalancer configuration to fix BeanPostProcessor warnings
 * and optimize load balancing behavior
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "true", matchIfMissing = false)
public class LoadBalancerConfiguration {

    /**
     * WebClient.Builder with load balancing - marked as INFRASTRUCTURE role
     * to avoid BeanPostProcessor conflicts
     */
    @Bean
    @LoadBalanced
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Custom ServiceInstanceListSupplier for regional load balancing
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
            ConfigurableApplicationContext context) {
        return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .withHealthChecks()
                .withCaching()
                .build(context);
    }
}
