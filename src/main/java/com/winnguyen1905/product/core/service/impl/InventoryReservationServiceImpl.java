package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.core.model.entity.Reservation;
import com.winnguyen1905.product.core.service.InventoryReservationService;
import com.winnguyen1905.product.core.service.ReservationService;
import com.winnguyen1905.product.exception.InsufficientInventoryException;
import com.winnguyen1905.product.exception.InventoryException;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import com.winnguyen1905.product.util.InventoryLockingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final InventoryRepository inventoryRepository;
    private final ReservationService reservationService;
    private final InventoryLockingUtils inventoryLockingUtils;

    @Override
    public Mono<Reservation> reserveInventory(Reservation reservation) {
        if (reservation == null || reservation.getItems() == null || reservation.getItems().isEmpty()) {
            return Mono.error(new InventoryException("No items to reserve"));
        }

        log.info("Processing reservation request for {} items", reservation.getItems().size());

        // Step 1: Create the reservation with PENDING status
        return reservationService.createReservation(reservation)
                .flatMap(createdReservation -> {
                    // Step 2: Process each item in the reservation
                    List<Mono<Boolean>> reservationOperations = createdReservation.getItems().stream()
                            .map(item -> processInventoryReservation(item, item.getQuantity()))
                            .collect(Collectors.toList());

                    // Step 3: Execute all reservation operations in parallel
                    return Flux.merge(reservationOperations)
                            .all(Boolean::booleanValue)
                            .flatMap(allSucceeded -> {
                                if (allSucceeded) {
                                    log.info("Successfully reserved all inventory items for reservation: {}", createdReservation.getId());
                                    return reservationService.updateReservationStatus(createdReservation.getId().toString(), "CONFIRMED")
                                            .onErrorResume(e -> {
                                                log.error("Error updating reservation status: {}", e.getMessage());
                                                return rollbackReservation(createdReservation)
                                                        .then(Mono.error(new InventoryException("Failed to update reservation status")));
                                            });
                                } else {
                                    log.warn("Some items could not be reserved for reservation: {}", createdReservation.getId());
                                    return rollbackReservation(createdReservation)
                                            .then(Mono.error(new InventoryException("Failed to reserve some items")));
                                }
                            })
                            .onErrorResume(e -> {
                                log.error("Error processing reservation: {}", e.getMessage());
                                return rollbackReservation(createdReservation)
                                        .then(Mono.error(new InventoryException("Failed to process reservation: " + e.getMessage())));
                            });
                });
    }

    @Override
    public Mono<Boolean> releaseInventory(Reservation reservation) {
        if (reservation == null || reservation.getItems() == null || reservation.getItems().isEmpty()) {
            return Mono.just(true); // Nothing to release
        }

        log.info("Processing release request for {} items", reservation.getItems().size());

        List<Mono<Boolean>> releaseOperations = reservation.getItems().stream()
                .map(item -> processInventoryRelease(item, item.getQuantity()))
                .collect(Collectors.toList());

        return Flux.merge(releaseOperations)
                .all(Boolean::booleanValue)
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Successfully released all inventory items for reservation: {}", reservation.getId());
                    } else {
                        log.warn("Some items could not be released for reservation: {}", reservation.getId());
                    }
                });
    }
    
    @Override
    public Mono<Boolean> confirmInventory(Reservation reservation) {
        log.info("Confirming inventory for reservation {}", reservation.getId());
        
        if (reservation == null || reservation.getItems() == null || reservation.getItems().isEmpty()) {
            return Mono.just(true); // Nothing to confirm
        }
        
        // For each item, move from reserved to sold state
        List<Mono<Boolean>> confirmOperations = reservation.getItems().stream()
                .map(item -> inventoryLockingUtils.executeWithOptimisticLockReturningBoolean(
                        item.getInventoryId(),
                        inventory -> {
                            int qty = item.getQuantity();
                            // Ensure we don't go below zero for reserved quantities
                            int actualReserved = Math.min(inventory.getQuantityReserved(), qty);
                            
                            inventory.setQuantityReserved(inventory.getQuantityReserved() - actualReserved);
                            inventory.setQuantitySold(inventory.getQuantitySold() + actualReserved);
                            
                            return inventory;
                        }))
                .collect(Collectors.toList());
                
        return Flux.merge(confirmOperations)
                .all(Boolean::booleanValue)
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Successfully confirmed all inventory items for reservation: {}", reservation.getId());
                    } else {
                        log.warn("Some items could not be confirmed for reservation: {}", reservation.getId());
                    }
                });
    }
    
    /**
     * Process inventory reservation for a single item with optimistic locking
     */
    private Mono<Boolean> processInventoryReservation(Reservation.ReservationItem item, Integer quantity) {
        if (item.getInventoryId() != null) {
            return reserveInventoryById(item.getInventoryId(), quantity);
        } else if (item.getSku() != null) {
            return reserveInventoryBySku(item.getSku(), quantity);
        } else {
            log.error("Cannot reserve inventory: missing both inventoryId and SKU");
            return Mono.just(false);
        }
    }
    
    /**
     * Process inventory release for a single item with optimistic locking
     */
    private Mono<Boolean> processInventoryRelease(Reservation.ReservationItem item, Integer quantity) {
        if (item.getInventoryId() != null) {
            return releaseInventoryById(item.getInventoryId(), quantity);
        } else if (item.getSku() != null) {
            return releaseInventoryBySku(item.getSku(), quantity);
        } else {
            log.error("Cannot release inventory: missing both inventoryId and SKU");
            return Mono.just(false);
        }
    }

    /**
     * Reserve inventory by ID with optimistic locking
     */
    private Mono<Boolean> reserveInventoryById(UUID id, Integer quantity) {
        return inventoryLockingUtils.executeWithOptimisticLockReturningBoolean(id, inventory -> {
            if (inventory.getQuantityAvailable() < quantity) {
                throw new InsufficientInventoryException(
                    "Insufficient quantity available", 
                    inventory.getSku(), 
                    inventory.getQuantityAvailable(), 
                    quantity
                );
            }
            
            inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
            inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
            return inventory;
        });
    }
    
    /**
     * Reserve inventory by SKU with optimistic locking
     */
    private Mono<Boolean> reserveInventoryBySku(String sku, Integer quantity) {
        return inventoryLockingUtils.executeWithOptimisticLockBySkuReturningBoolean(sku, inventory -> {
            if (inventory.getQuantityAvailable() < quantity) {
                throw new InsufficientInventoryException(
                    "Insufficient quantity available", 
                    inventory.getSku(), 
                    inventory.getQuantityAvailable(), 
                    quantity
                );
            }
            
            inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
            inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
            return inventory;
        });
    }

    /**
     * Release inventory by ID with optimistic locking
     */
    private Mono<Boolean> releaseInventoryById(UUID id, Integer quantity) {
        return inventoryLockingUtils.executeWithOptimisticLockReturningBoolean(id, inventory -> {
            if (inventory.getQuantityReserved() < quantity) {
                log.warn("Cannot release more than reserved. ID: {}, Requested: {}, Reserved: {}", 
                        id, quantity, inventory.getQuantityReserved());
                // Adjust quantity to match what's reserved to avoid negative values
                int adjustedQuantity = inventory.getQuantityReserved();
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() + adjustedQuantity);
                inventory.setQuantityReserved(0);
            } else {
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
                inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
            }
            return inventory;
        });
    }
    
    /**
     * Release inventory by SKU with optimistic locking
     */
    private Mono<Boolean> releaseInventoryBySku(String sku, Integer quantity) {
        return inventoryLockingUtils.executeWithOptimisticLockBySkuReturningBoolean(sku, inventory -> {
            if (inventory.getQuantityReserved() < quantity) {
                log.warn("Cannot release more than reserved. SKU: {}, Requested: {}, Reserved: {}", 
                        sku, quantity, inventory.getQuantityReserved());
                // Adjust quantity to match what's reserved to avoid negative values
                int adjustedQuantity = inventory.getQuantityReserved();
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() + adjustedQuantity);
                inventory.setQuantityReserved(0);
            } else {
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
                inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
            }
            return inventory;
        });
    }
    
    /**
     * Rollback all items in a reservation
     */
    private Mono<Void> rollbackReservation(Reservation reservation) {
        log.info("Rolling back reservation: {}", reservation.getId());
        return releaseInventory(reservation)
                .flatMap(released -> reservationService.updateReservationStatus(reservation.getId().toString(), "CANCELLED")
                        .then(Mono.empty()));
    }
}
