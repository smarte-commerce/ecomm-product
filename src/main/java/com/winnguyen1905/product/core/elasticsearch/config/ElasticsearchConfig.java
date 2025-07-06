package com.winnguyen1905.product.core.elasticsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.winnguyen1905.product.core.elasticsearch.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port:9200}")
    private int elasticsearchPort;

    @Value("${elasticsearch.username:}")
    private String username;

    @Value("${elasticsearch.password:}")
    private String password;

    @Value("${elasticsearch.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${elasticsearch.connection.timeout:10000}")
    private int connectionTimeout;

    @Value("${elasticsearch.socket.timeout:30000}")
    private int socketTimeout;

    @Override
    public ClientConfiguration clientConfiguration() {
        String hostAndPort = elasticsearchHost + ":" + elasticsearchPort;
        
        try {
            var builder = ClientConfiguration.builder()
                .connectedTo(hostAndPort);

            // Add configuration based on available methods
            if (isAuthenticationConfigured()) {
                try {
                    builder.withBasicAuth(username, password);
                } catch (Exception e) {
                    log.warn("Basic auth configuration failed, proceeding without authentication: {}", e.getMessage());
                }
            }

            ClientConfiguration config = builder.build();
            
            log.info("Elasticsearch client configured - Host: {}, Auth: {}", 
                    hostAndPort, isAuthenticationConfigured());
            
            return config;
            
        } catch (Exception e) {
            log.error("Failed to configure Elasticsearch client: {}", e.getMessage());
            // Fallback to basic configuration
            return ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .build();
        }
    }
    
    private boolean isAuthenticationConfigured() {
        return username != null && !username.trim().isEmpty() && 
               password != null && !password.trim().isEmpty();
    }
} 
