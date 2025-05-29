package com.winnguyen1905.product.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.JsonValue;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class CommonUtils {
  public static <T> Stream<T> stream(Collection<T> collection) {
    if (CollectionUtils.isEmpty(collection)) {
      return Stream.empty();
    } else {
      return collection.stream();
    }
  }

  public static Object convertKeyValueToObject(Object keyValueObject) {
    ObjectMapper objectMapper = new ObjectMapper(); 
    String jsonString = keyValueObject.toString();
    try {
      return objectMapper.readValue(jsonString, Object.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonValue fromObject(Object object) {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.valueToTree(object);
  }

  public static <T> Mono<T> toMono(Optional<T> optional) {
    return optional.map(Mono::just).orElseGet(Mono::empty);
  }

  public static <T> Mono<T> handleError(Throwable throwable, String operation) {
    log.error("Error during {}: {}", operation, throwable.getMessage());
    return Mono.error(throwable);
  }

  public static <T> Mono<T> handleError(Throwable throwable) {
    log.error("Error during {}: {}", throwable.getMessage());
    return Mono.error(throwable);
  }

  public static <T> Mono<T> handleErrorWithCustomMessage(Throwable throwable, String operation, String customMessage) {
    log.error("Error during {} - {}: {}", operation, customMessage, throwable.getMessage());
    return Mono.error(throwable);
  }
}
