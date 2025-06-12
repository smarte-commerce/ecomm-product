package com.winnguyen1905.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

/**
 * Configuration class for Redis connection and template setup.
 * Supports both reactive and non-reactive Redis operations.
 */
@Slf4j
@Configuration
public class RedisConfig implements MessageListener, ApplicationEventPublisherAware {

  private ApplicationEventPublisher eventPublisher;

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.eventPublisher = applicationEventPublisher;
  }

  // Redis connection properties with default values
  private static final String DEFAULT_REDIS_HOST = "localhost";
  private static final int DEFAULT_REDIS_PORT = 6379;
  private static final long DEFAULT_TIMEOUT_MS = 2000L;
  private static final String DEFAULT_PASSWORD = "mypassword";

  @Value("${spring.data.redis.host:" + DEFAULT_REDIS_HOST + "}")
  private String redisHost;

  @Value("${spring.data.redis.port:${spring.redis.port:${REDIS_PORT:" + DEFAULT_REDIS_PORT + "}}}")
  private int redisPort;

  @Value("${spring.data.redis.timeout:${spring.redis.timeout:" + DEFAULT_TIMEOUT_MS + "}}")
  private long redisTimeout;

  @Value("${spring.data.redis.password:${spring.redis.password:${REDIS_PASSWORD:" + DEFAULT_PASSWORD + "}}}")
  private String redisPassword;

  /**
   * Creates a reactive Redis connection factory.
   */
  @Bean
  public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
    log.info("Creating ReactiveRedisConnectionFactory for Redis at {}:{}", redisHost, redisPort);
    return new LettuceConnectionFactory(redisStandaloneConfiguration(), lettuceClientConfiguration());
  }

  /**
   * Creates a non-reactive Redis connection factory.
   */
  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    log.info("Creating RedisConnectionFactory for Redis at {}:{}", redisHost, redisPort);
    return new LettuceConnectionFactory(redisStandaloneConfiguration(), lettuceClientConfiguration());
  }

  /**
   * Configures Redis standalone connection details.
   */
  private RedisStandaloneConfiguration redisStandaloneConfiguration() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setHostName(redisHost);
    config.setPort(redisPort);
    if (redisPassword != null && !redisPassword.isEmpty()) {
      config.setPassword(RedisPassword.of(redisPassword));
    }
    return config;
  }

  /**
   * Configures Lettuce client settings.
   */
  private LettuceClientConfiguration lettuceClientConfiguration() {
    return LettuceClientConfiguration.builder()
        .commandTimeout(Duration.ofMillis(redisTimeout))
        .clientOptions(ClientOptions.builder()
            .autoReconnect(true)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .socketOptions(SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(redisTimeout))
                .keepAlive(true)
                .build())
            .timeoutOptions(TimeoutOptions.enabled(Duration.ofMillis(redisTimeout)))
            .build())
        .build();
  }

  /**
   * Creates a reactive Redis template for Object serialization.
   */
  @Bean
  public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory factory) {
    log.debug("Creating ReactiveRedisTemplate");

    Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
    RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder = RedisSerializationContext
        .newSerializationContext(new StringRedisSerializer());

    RedisSerializationContext<String, Object> context = builder
        .value(serializer)
        .hashValue(serializer)
        .hashKey(serializer)
        .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }

  /**
   * Creates a non-reactive Redis template for Object serialization.
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    log.debug("Creating RedisTemplate");

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    // Configure serializers
    Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }

  /**
   * Provides reactive value operations for String values.
   */
  @Bean
  public ReactiveValueOperations<String, String> reactiveValueOperations(
      ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
    return reactiveRedisTemplate.opsForValue();
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String expiredKey = new String(message.getBody());
    log.info("Key expired: {}", expiredKey);

    // Check if it's a reservation key
    if (expiredKey.startsWith("reservation:")) {
      String reservationId = expiredKey.split(":")[1];
      log.info("Processing expired reservation: {}", reservationId);
      
      // Publish an event instead of directly calling the service
      eventPublisher.publishEvent(new ReservationExpiredEvent(UUID.fromString(reservationId)));
    }
  }
}
