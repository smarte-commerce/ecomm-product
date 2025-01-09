package com.winnguyen1905.product.util;

import java.util.Optional;
import java.util.UUID;

import com.winnguyen1905.product.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ExtractorUtils {
  public static <T> T fromOptional(Optional<T> optional, String errorMessage) {
    return optional.orElseThrow(() -> new ResourceNotFoundException(
        errorMessage != null ? errorMessage : "Resource not found for optional extract!"));
  }

  public static Mono<UUID> currentUserId() {
    return Mono.just(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    // return SecurityUtils.getCurrentUserId()
    //     .switchIfEmpty(Mono.just(UUID.randomUUID()));
  }

}
