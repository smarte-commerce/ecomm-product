package com.winnguyen1905.product.core.repository;

import com.winnguyen1905.product.core.model.entity.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReservationRepository {
  private static final String RESERVATION_KEY_PREFIX = "reservation:";
  private static final Duration DEFAULT_TTL = Duration.ofHours(24);

  private final ReactiveValueOperations<String, String> valueOperations;
  private final ObjectMapper objectMapper;

  public Mono<Reservation> save(Reservation reservation) {
    String key = getKey(reservation.getId().toString());

    // Set timestamps
    Instant now = Instant.now();
    if (reservation.getCreatedAt() == null) {
      reservation.setCreatedAt(now);
      reservation.setVersion(1L);
    } else {
      reservation.setUpdatedAt(now);
      reservation.setVersion(reservation.getVersion() + 1);
    }

    try {
      String json = objectMapper.writeValueAsString(reservation);
      Duration ttl = reservation.getExpiresAt() != null
          ? Duration.between(now, reservation.getExpiresAt())
          : DEFAULT_TTL;

      return valueOperations.set(key, json, ttl)
          .thenReturn(reservation);
    } catch (JsonProcessingException e) {
      return Mono.error(new RuntimeException("Failed to serialize reservation", e));
    }
  }

  public Mono<Reservation> findById(String id) {
    return valueOperations.get(getKey(id))
        .flatMap(json -> {
          try {
            return Mono.just(objectMapper.readValue(json, Reservation.class));
          } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to deserialize reservation", e));
          }
        });
  }

  public Mono<Boolean> delete(String id) {
    return valueOperations.delete(getKey(id));
  }

  public Mono<Boolean> existsById(String id) {
    return valueOperations.get(getKey(id)).hasElement();
  }

  private String getKey(String id) {
    return RESERVATION_KEY_PREFIX + id;
  }
}
