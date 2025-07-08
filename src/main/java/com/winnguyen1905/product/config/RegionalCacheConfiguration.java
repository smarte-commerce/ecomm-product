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
import org.springframework.cache.interceptor.CacheResolver;
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
import java.util.HashMap;
import java.util.Map;

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
    return new RegionalCacheManager();
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
    return new RegionalRedisTemplate();
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
    Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL);
    jsonSerializer.setObjectMapper(objectMapper);

    // Set serializers
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jsonSerializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jsonSerializer);

    template.afterPropertiesSet();
    return template;
  }

  /**
   * Regional cache manager that routes operations to region-specific Redis
   * instances
   */
  public class RegionalCacheManager implements CacheManager {

    private final Map<RegionPartition, RedisCacheManager> regionalManagers = new HashMap<>();
    private final RedisCacheManager defaultManager;

    public RegionalCacheManager() {
      // Initialize regional cache managers
      regionalManagers.put(RegionPartition.US, createRegionalCacheManager(usRedisConnectionFactory()));
      regionalManagers.put(RegionPartition.EU, createRegionalCacheManager(euRedisConnectionFactory()));
      regionalManagers.put(RegionPartition.ASIA, createRegionalCacheManager(asiaRedisConnectionFactory()));

      // Default manager (US)
      defaultManager = regionalManagers.get(RegionPartition.US);

      log.info("Initialized regional cache managers for {} regions", regionalManagers.size());
    }

    @Override
    public org.springframework.cache.Cache getCache(String name) {
      RegionPartition region = getCurrentRegion();
      RedisCacheManager manager = regionalManagers.getOrDefault(region, defaultManager);

      log.debug("Getting cache '{}' for region: {}", name, region);
      return manager.getCache(name);
    }

    @Override
    public java.util.Collection<String> getCacheNames() {
      return defaultManager.getCacheNames();
    }

    private RedisCacheManager createRegionalCacheManager(RedisConnectionFactory connectionFactory) {
      RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
          .entryTtl(Duration.ofHours(24)) // Default 24 hour TTL
          .serializeKeysWith(RedisSerializationContext.SerializationPair
              .fromSerializer(new StringRedisSerializer()))
          .serializeValuesWith(RedisSerializationContext.SerializationPair
              .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
          .disableCachingNullValues();

      return RedisCacheManager.builder(connectionFactory)
          .cacheDefaults(config)
          .build();
    }

    private RegionPartition getCurrentRegion() {
      try {
        // Try to get region from thread local context
        RegionPartition region = RegionalDataSourceConfiguration.RegionalContext.getCurrentRegion();
        if (region != null) {
          return region;
        }

        // Try to get region from request headers
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
          String regionCode = attributes.getRequest().getHeader("X-Region-Code");
          if (regionCode != null && !regionCode.trim().isEmpty()) {
            try {
              return RegionPartition.fromCode(regionCode);
            } catch (Exception e) {
              log.debug("Invalid region code in header: {}", regionCode);
            }
          }
        }
      } catch (Exception e) {
        log.debug("Error determining current region for cache: {}", e.getMessage());
      }

      // Default to US
      return RegionPartition.US;
    }
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
   * Regional Redis template that routes operations to correct region
   */
  public class RegionalRedisTemplate extends RedisTemplate<String, Object> {

    private final Map<RegionPartition, RedisTemplate<String, Object>> regionalTemplates = new HashMap<>();
    private final RedisTemplate<String, Object> defaultTemplate;

    public RegionalRedisTemplate() {
      regionalTemplates.put(RegionPartition.US, usRedisTemplate());
      regionalTemplates.put(RegionPartition.EU, euRedisTemplate());
      regionalTemplates.put(RegionPartition.ASIA, asiaRedisTemplate());
      defaultTemplate = regionalTemplates.get(RegionPartition.US);
    }

    @Override
    public org.springframework.data.redis.core.ValueOperations<String, Object> opsForValue() {
      RegionPartition region = getCurrentRegion();
      RedisTemplate<String, Object> template = regionalTemplates.getOrDefault(region, defaultTemplate);
      return template.opsForValue();
    }

    @Override
    public org.springframework.data.redis.core.HashOperations<String, Object, Object> opsForHash() {
      RegionPartition region = getCurrentRegion();
      RedisTemplate<String, Object> template = regionalTemplates.getOrDefault(region, defaultTemplate);
      return template.opsForHash();
    }

    @Override
    public org.springframework.data.redis.core.ListOperations<String, Object> opsForList() {
      RegionPartition region = getCurrentRegion();
      RedisTemplate<String, Object> template = regionalTemplates.getOrDefault(region, defaultTemplate);
      return template.opsForList();
    }

    @Override
    public org.springframework.data.redis.core.SetOperations<String, Object> opsForSet() {
      RegionPartition region = getCurrentRegion();
      RedisTemplate<String, Object> template = regionalTemplates.getOrDefault(region, defaultTemplate);
      return template.opsForSet();
    }

    @Override
    public org.springframework.data.redis.core.ZSetOperations<String, Object> opsForZSet() {
      RegionPartition region = getCurrentRegion();
      RedisTemplate<String, Object> template = regionalTemplates.getOrDefault(region, defaultTemplate);
      return template.opsForZSet();
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
        log.debug("Error determining region for Redis operation: {}", e.getMessage());
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
