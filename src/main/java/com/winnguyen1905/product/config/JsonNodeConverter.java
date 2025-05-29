package com.winnguyen1905.product.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter(autoApply = false)
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {
  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(JsonNode jsonNode) {
    if (jsonNode == null) {
      return null;
    }
    try {
      return mapper.writeValueAsString(jsonNode);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert JsonNode to String", e);
    }
  }

  @Override
  public JsonNode convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    try {
      return mapper.readTree(dbData);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert String to JsonNode", e);
    }
  }
}
