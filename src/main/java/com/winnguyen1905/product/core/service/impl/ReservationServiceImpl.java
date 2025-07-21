package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.core.model.entity.Reservation;
import com.winnguyen1905.product.core.service.ReservationService;
import com.winnguyen1905.product.exception.ReservationException;
import com.winnguyen1905.product.persistance.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

  private final ReservationRepository reservationRepository;

  private static final Duration DEFAULT_RESERVATION_DURATION = Duration.ofMinutes(30);
  private static final int MAX_RETRIES = 3;

  @Override
  public Mono<Reservation> createReservation(Reservation reservation) {
    if (reservation.getId() == null) {
      reservation.setId(UUID.randomUUID());
    }

    // Set expiration time if not set
    if (reservation.getExpiresAt() == null) {
      reservation.setExpiresAt(Instant.now().plus(DEFAULT_RESERVATION_DURATION));
    }

    reservation.setStatus("PENDING");

    log.info("Creating reservation: {}", reservation.getId());
    return reservationRepository.save(reservation);
  }

  @Override
  public Mono<Reservation> getReservation(String id) {
    log.debug("Getting reservation: {}", id);
    return reservationRepository.findById(id)
        .switchIfEmpty(Mono.error(new ReservationException("Reservation not found: " + id)));
  }

  @Override
  public Mono<Reservation> updateReservation(Reservation reservation) {
    return reservationRepository.existsById(reservation.getId().toString())
        .flatMap(exists -> {
          if (!exists) {
            return Mono
                .error(new ReservationException("Cannot update non-existent reservation: " + reservation.getId()));
          }

          log.info("Updating reservation: {}", reservation.getId());
          return reservationRepository.save(reservation);
        });
  }

  @Override
  public Mono<Reservation> updateReservationStatus(String reservationId, String status) {
    log.info("Updating reservation: {} to status: {}", reservationId, status);

    return reservationRepository.findById(reservationId)
        .switchIfEmpty(Mono.error(new ReservationException("Reservation not found: " + reservationId)))
        .flatMap(reservation -> {
          reservation.setStatus(status);
          if ("CONFIRMED".equals(status)) {
            reservation.setConfirmedAt(Instant.now());
          } else if ("CANCELLED".equals(status)) {
            reservation.setCancelledAt(Instant.now());
          }
          return reservationRepository.save(reservation);
        });
  }

  @Override
  public Mono<Reservation> confirmReservation(String reservationId, String orderId) {
    log.info("Confirming reservation: {} for order: {}", reservationId, orderId);

    return reservationRepository.findById(reservationId)
        .switchIfEmpty(Mono.error(new ReservationException("Reservation not found: " + reservationId)))
        .flatMap(reservation -> {
          if ("CONFIRMED".equals(reservation.getStatus())) {
            log.info("Reservation {} already confirmed", reservationId);
            return Mono.just(reservation);
          }
          if ("CANCELLED".equals(reservation.getStatus())) {
            return Mono.error(new IllegalStateException("Cannot confirm a cancelled reservation"));
          }
          if ("EXPIRED".equals(reservation.getStatus())) {
            return Mono.error(new IllegalStateException("Cannot confirm an expired reservation"));
          }

          // Set order ID and update status
          reservation.setOrderId(orderId);
          reservation.setStatus("CONFIRMED");
          reservation.setConfirmedAt(Instant.now());
          return reservationRepository.save(reservation);
        });
  }

  @Override
  public Mono<Boolean> cancelReservation(String reservationId) {
    log.info("Cancelling reservation: {}", reservationId);

    return reservationRepository.findById(reservationId)
        .switchIfEmpty(Mono.error(new ReservationException("Reservation not found: " + reservationId)))
        .flatMap(reservation -> {
          // Check if already cancelled
          if ("CANCELLED".equals(reservation.getStatus())) {
            log.info("Reservation {} already cancelled", reservationId);
            return Mono.just(true);
          }

          // Cannot cancel confirmed reservation
          if ("CONFIRMED".equals(reservation.getStatus())) {
            return Mono.error(new IllegalStateException("Cannot cancel a confirmed reservation"));
          }

          // Update reservation status to CANCELLED
          reservation.setStatus("CANCELLED");
          reservation.setCancelledAt(Instant.now());
          return reservationRepository.save(reservation).thenReturn(true);
        });
  }

  @Override
  public Mono<Boolean> isReservationValid(String id) {
    return reservationRepository.findById(id)
        .map(reservation -> {
          boolean isValid = "PENDING".equals(reservation.getStatus()) ||
              "CONFIRMED".equals(reservation.getStatus());

          if (isValid && reservation.getExpiresAt() != null) {
            isValid = Instant.now().isBefore(reservation.getExpiresAt());
          }

          return isValid;
        })
        .defaultIfEmpty(false);
  }
}
