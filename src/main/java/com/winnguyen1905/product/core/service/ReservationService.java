package com.winnguyen1905.product.core.service;

import com.winnguyen1905.product.core.model.entity.Reservation;
import reactor.core.publisher.Mono;

public interface ReservationService {
    Mono<Reservation> createReservation(Reservation reservation);
    Mono<Reservation> confirmReservation(String reservationId, String orderId);
    Mono<Boolean> cancelReservation(String reservationId);
    Mono<Reservation> getReservation(String reservationId);
    
    // New method to handle reservation updates
    Mono<Reservation> updateReservationStatus(String reservationId, String status);
}
