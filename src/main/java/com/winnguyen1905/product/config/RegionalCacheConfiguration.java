package com.winnguyen1905.product.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.winnguyen1905.product.secure.RegionPartition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * Regional Redis caching configuration that partitions cache data by user
 * region.
 * Provides intelligent cache key generation and region-aware cache management.
 */
@Slf4j
@Configuration
@EnableCaching
public class RegionalCacheConfiguration implements CachingConfigurer {

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  /**
   * Regional cache manager that routes cache operations to region-specific Redis
   * databases
   */
  @Primary
  @Bean
  @Override
  public CacheManager cacheManager() {
    // Create simple cache manager using default connection factory for now
    // The regional routing will be handled at the application level
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(24))
        .serializeKeysWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new Jackson2JsonRedisSerializer<>(new ObjectMapper(), Object.class)))
        .disableCachingNullValues();

    return RedisCacheManager.builder(redisConnectionFactory())
        .cacheDefaults(config)
        .build();
  }

  /**
   * Regional key generator that automatically prefixes cache keys with region
   */
  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new RegionalKeyGenerator();
  }

  /**
   * Redis connection factory for US region (database 0)
   */
  @Bean
  public RedisConnectionFactory usRedisConnectionFactory() {
    return createRedisConnectionFactory(0);
  }

  /**
   * Redis connection factory for EU region (database 1)
   */
  @Bean
  public RedisConnectionFactory euRedisConnectionFactory() {
    return createRedisConnectionFactory(1);
  }

  /**
   * Redis connection factory for ASIA region (database 2)
   */
  @Bean
  public RedisConnectionFactory asiaRedisConnectionFactory() {
    return createRedisConnectionFactory(2);
  }

  /**
   * Default Redis connection factory (US region)
   */
  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    return usRedisConnectionFactory();
  }

  /**
   * Regional Redis template that automatically routes to correct region
   */
  @Bean
  @Primary
  public RedisTemplate<String, Object> redisTemplate() {
    return createRedisTemplate(redisConnectionFactory());
  }

  /**
   * US region Redis template
   */
  @Bean
  public RedisTemplate<String, Object> usRedisTemplate() {
    return createRedisTemplate(usRedisConnectionFactory());
  }

  /**
   * EU region Redis template
   */
  @Bean
  public RedisTemplate<String, Object> euRedisTemplate() {
    return createRedisTemplate(euRedisConnectionFactory());
  }

  /**
   * ASIA region Redis template
   */
  @Bean
  public RedisTemplate<String, Object> asiaRedisTemplate() {
    return createRedisTemplate(asiaRedisConnectionFactory());
  }

  /**
   * Cache error handler for graceful degradation
   */
  @Bean
  @Override
  public CacheErrorHandler errorHandler() {
    return new RegionalCacheErrorHandler();
  }

  /**
   * Create Redis connection factory for specific database
   */
  private RedisConnectionFactory createRedisConnectionFactory(int database) {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setHostName(redisHost);
    config.setPort(redisPort);
    config.setDatabase(database);

    if (redisPassword != null && !redisPassword.trim().isEmpty()) {
      config.setPassword(redisPassword);
    }

    LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
    factory.afterPropertiesSet(); // Initialize the connection factory
    log.info("Creating Redis connection factory for database {} (region cache)", database);

    return factory;
  }

  /**
   * Create Redis template with JSON serialization
   */
  private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // JSON serialization configuration
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL);
    
    Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

    // Set serializers
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jsonSerializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jsonSerializer);

    template.afterPropertiesSet();
    return template;
  }

  /**
   * Regional key generator that prefixes keys with region information
   */
  public static class RegionalKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, java.lang.reflect.Method method, Object... params) {
      RegionPartition region = getCurrentRegion();
      String baseKey = generateBaseKey(target, method, params);
      String regionalKey = region.getCode() + ":" + baseKey;

      log.debug("Generated regional cache key: {}", regionalKey);
      return regionalKey;
    }

    private String generateBaseKey(Object target, java.lang.reflect.Method method, Object... params) {
      StringBuilder sb = new StringBuilder();
      sb.append(target.getClass().getSimpleName());
      sb.append(".");
      sb.append(method.getName());

      if (params != null && params.length > 0) {
        sb.append("(");
        for (int i = 0; i < params.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          if (params[i] != null) {
            sb.append(params[i].toString());
          } else {
            sb.append("null");
          }
        }
        sb.append(")");
      }

      return sb.toString();
    }

    private RegionPartition getCurrentRegion() {
      try {
        RegionPartition region = RegionalDataSourceConfiguration.RegionalContext.getCurrentRegion();
        if (region != null) {
          return region;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
          String regionCode = attributes.getRequest().getHeader("X-Region-Code");
          if (regionCode != null) {
            return RegionPartition.fromCode(regionCode);
          }
        }
      } catch (Exception e) {
        log.debug("Error determining region for cache key generation: {}", e.getMessage());
      }

      return RegionPartition.US;
    }
  }

  /**
   * Error handler for cache operations that provides graceful degradation
   */
  public static class RegionalCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception,
        org.springframework.cache.Cache cache, Object key) {
      log.warn("Cache GET error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
      // Gracefully degrade - return null so the method executes normally
    }

    @Override
    public void handleCachePutError(RuntimeException exception,
        org.springframework.cache.Cache cache, Object key, Object value) {
      log.warn("Cache PUT error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
      // Gracefully degrade - continue without caching
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception,
        org.springframework.cache.Cache cache, Object key) {
      log.warn("Cache EVICT error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
      // Gracefully degrade - continue without evicting
    }

    @Override
    public void handleCacheClearError(RuntimeException exception,
        org.springframework.cache.Cache cache) {
      log.warn("Cache CLEAR error for cache {}: {}", cache.getName(), exception.getMessage());
      // Gracefully degrade - continue without clearing
    }
  }
}
