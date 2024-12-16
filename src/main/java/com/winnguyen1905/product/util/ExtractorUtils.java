package com.winnguyen1905.product.util;

import java.util.Optional;
import java.util.UUID;

import com.winnguyen1905.product.config.SecurityUtils;
import com.winnguyen1905.product.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ExtractorUtils {
  public static <T> T fromOptional(Optional<T> optional, String errorMessage) {
    if (optional.isPresent() && optional.get() instanceof T t)
      return t;
    else
      throw new ResourceNotFoundException(
          errorMessage != null ? errorMessage : "Resource not found for optional extract !");
  }

  public static UUID currentUserId() {
    return 
        SecurityUtils.getCurrentUserId()
        .onErrorResume(null)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Current user ID not found")))
        .block();
  }

}
