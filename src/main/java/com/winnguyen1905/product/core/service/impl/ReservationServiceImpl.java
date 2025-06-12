package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.core.model.entity.Reservation;
import com.winnguyen1905.product.core.repository.ReservationRepository;
import com.winnguyen1905.product.core.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

  private final ReservationRepository reservationRepository;

  @Override
  public Mono<Reservation> createReservation(Reservation reservation) {
    if (reservation.getId() == null) {
      reservation.setId(UUID.randomUUID());
    }
    reservation.setStatus("PENDING");

    log.info("Creating reservation for {} items", reservation.getItems().size());

    // Save the reservation with PENDING status
    return reservationRepository.save(reservation)
        .doOnSuccess(res -> log.info("Successfully created reservation {}", res.getId()))
        .onErrorResume(e -> {
          log.error("Error creating reservation: {}", e.getMessage());
          return Mono.error(new RuntimeException("Failed to create reservation: " + e.getMessage()));
        });
  }

  @Override
  public Mono<Reservation> updateReservationStatus(String reservationId, String status) {
    log.info("Updating reservation: {} to status: {}", reservationId, status);

    return reservationRepository.findById(reservationId)
        .switchIfEmpty(Mono.error(new RuntimeException("Reservation not found: " + reservationId)))
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
        .switchIfEmpty(Mono.error(new RuntimeException("Reservation not found: " + reservationId)))
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
          return updateReservationStatus(reservationId, "CONFIRMED");
        })
        .onErrorResume(e -> {
          log.error("Error confirming reservation {}: {}", reservationId, e.getMessage());
          return Mono.error(new RuntimeException("Failed to confirm reservation: " + e.getMessage()));
        });
  }

  @Override
  public Mono<Boolean> cancelReservation(String reservationId) {
    log.info("Cancelling reservation: {}", reservationId);

    return reservationRepository.findById(reservationId)
        .switchIfEmpty(Mono.error(new RuntimeException("Reservation not found: " + reservationId)))
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
          return updateReservationStatus(reservationId, "CANCELLED")
              .thenReturn(true);
        })
        .onErrorResume(e -> {
          log.error("Error cancelling reservation {}: {}", reservationId, e.getMessage());
          return Mono.error(new RuntimeException("Failed to cancel reservation: " + e.getMessage()));
        });
  }

  @Override
  public Mono<Reservation> getReservation(String reservationId) {
    return reservationRepository.findById(reservationId);
  }
}
