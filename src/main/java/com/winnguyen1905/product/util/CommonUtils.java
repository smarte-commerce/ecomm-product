package com.winnguyen1905.product.util;

import java.util.Collection;
import java.util.stream.Stream;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
}
