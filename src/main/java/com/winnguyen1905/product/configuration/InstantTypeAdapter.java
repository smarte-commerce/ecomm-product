package com.winnguyen1905.product.configuration;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.shaded.gson.JsonDeserializationContext;
import com.nimbusds.jose.shaded.gson.JsonDeserializer;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.gson.JsonSerializationContext;
import com.nimbusds.jose.shaded.gson.JsonSerializer;

@Component
public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss a dd-MM-yyyy");

  @Override
  public JsonElement serialize(Instant Instant, Type srcType,
      JsonSerializationContext context) {

    return new JsonPrimitive(formatter.format(Instant));
  }

  @Override
  public Instant deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    return Instant.parse(json.getAsString());
  }
}
