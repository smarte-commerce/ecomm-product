package com.winnguyen1905.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.winnguyen1905.promotion.persistance.repository")
@ComponentScan(basePackages = { "com.winnguyen1905.product.core.service" })
public class ElasticsearchConfig
//  extends ElasticsearchConfiguration 
 {

  // @Value("${spring.elasticsearch.uris}")
  // private String elasticsearchUrl;

  // @Override
  // public ClientConfiguration clientConfiguration() {
  //     return ClientConfiguration.builder()
  //             .connectedTo(elasticsearchUrl)
  //             .build();
  // }
  
}
