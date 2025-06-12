package com.winnguyen1905.product.core.service;

import com.winnguyen1905.product.core.model.entity.Reservation;
import reactor.core.publisher.Mono;

public interface InventoryReservationService {
    /**
     * Process inventory reservation for the given reservation
     * @param reservation The reservation to process
     * @return The processed reservation
     */
    Mono<Reservation> reserveInventory(Reservation reservation);
    
    /**
     * Release inventory for the given reservation
     * @param reservation The reservation to release inventory for
     * @return true if successful, false otherwise
     */
    Mono<Boolean> releaseInventory(Reservation reservation);
    
    /**
     * Confirm inventory for the given reservation
     * @param reservation The reservation to confirm inventory for
     * @return true if successful, false otherwise
     */
    Mono<Boolean> confirmInventory(Reservation reservation);
}
