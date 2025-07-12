package com.winnguyen1905.product.core.service;

import com.winnguyen1905.product.core.model.entity.Reservation;
import reactor.core.publisher.Mono;

public interface InventoryReservationService {
    
    /**
     * Reserve inventory for items in a reservation
     */
    Mono<Reservation> reserveInventory(Reservation reservation);
    
    /**
     * Release inventory previously reserved
     */
    Mono<Boolean> releaseInventory(Reservation reservation);
    
    /**
     * Confirm inventory reservation and move to sold state
     */
    Mono<Boolean> confirmInventory(Reservation reservation);
}
