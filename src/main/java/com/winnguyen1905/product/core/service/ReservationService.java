package com.winnguyen1905.product.core.service;

import com.winnguyen1905.product.core.model.entity.Reservation;
import reactor.core.publisher.Mono;

public interface ReservationService {
  /**
   * Create a new reservation
   */
  Mono<Reservation> createReservation(Reservation reservation);

  /**
   * Get reservation by ID
   */
  Mono<Reservation> getReservation(String id);

  /**
   * Update an existing reservation
   */
  Mono<Reservation> updateReservation(Reservation reservation);

  /**
   * Update reservation status
   */
  Mono<Reservation> updateReservationStatus(String reservationId, String status);

  /**
   * Confirm reservation and associate with order
   */
  Mono<Reservation> confirmReservation(String reservationId, String orderId);

  /**
   * Cancel reservation
   */
  Mono<Boolean> cancelReservation(String reservationId);

  /**
   * Check if reservation is valid
   */
  Mono<Boolean> isReservationValid(String id);
}
