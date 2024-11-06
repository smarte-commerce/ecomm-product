package com.winnguyen1905.product.configuration;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.winnguyen1905.product.core.service.RedisService;

import jakarta.annotation.PostConstruct;

@Configuration
public class RedisConfiguration {
  
  @PostConstruct
  public void init(RedisService redisService) {
    redisService.initializeRedis();
  }

  @Bean
  ConcurrentLinkedDeque<?> redisQueue(){
    return new ConcurrentLinkedDeque<>();
  }

  @Bean
  ExecutorService executorService(){
    return Executors.newFixedThreadPool(2);
  }

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
    // jedisConFactory.setHostName("${host.url}");
    // jedisConFactory.setPort(6379);
    return jedisConFactory;
  }

  @Bean
  RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(jedisConnectionFactory());
    return template;
  }
}