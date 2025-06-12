package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.core.model.entity.Reservation;
import com.winnguyen1905.product.core.repository.ReservationRepository;
import com.winnguyen1905.product.core.service.InventoryReservationService;
import com.winnguyen1905.product.core.service.InventoryValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional
public class InventoryReservationServiceImpl implements InventoryReservationService {

  private final ReservationRepository reservationRepository;
  private final InventoryValidationService inventoryValidationService;

  public InventoryReservationServiceImpl(ReservationRepository reservationRepository,
      @Lazy InventoryValidationService inventoryValidationService) {
    this.reservationRepository = reservationRepository;
    this.inventoryValidationService = inventoryValidationService;
  }

  @Override
  public Mono<Reservation> reserveInventory(Reservation reservation) {
    log.info("Processing reservation for {} items", reservation.getItems().size());

    return Flux.fromIterable(reservation.getItems())
        .flatMap(item -> inventoryValidationService.validateAndGetInventory(item.getSku(), item.getQuantity())
            .switchIfEmpty(Mono.error(new RuntimeException("Inventory not found for SKU: " + item.getSku())))
            .flatMap(inv -> {
              if (inv.getQuantityAvailable() < item.getQuantity()) {
                return Mono.error(new RuntimeException("Insufficient stock for SKU: " + item.getSku() +
                    ". Available: " + inv.getQuantityAvailable() +
                    ", Requested: " + item.getQuantity()));
              }
              return inventoryValidationService.reserveInventory(item.getSku(), item.getQuantity())
                  .then(Mono.just(true));
            }))
        .then(Mono.just(reservation))
        .flatMap(reservationRepository::save)
        .doOnSuccess(res -> log.info("Successfully processed reservation {}", res.getId()))
        .onErrorResume(e -> {
          log.error("Error processing reservation: {}", e.getMessage());
          return Mono.error(new RuntimeException("Failed to process reservation: " + e.getMessage()));
        });
  }

  @Override
  public Mono<Boolean> releaseInventory(Reservation reservation) {
    log.info("Releasing inventory for reservation {}", reservation.getId());
    // For release, we might need to update the implementation based on actual
    // requirements
    // For now, we'll just return success as the actual release might be handled by
    // a different service
    return Mono.just(true)
        .doOnSuccess(v -> log.info("Release request processed for reservation {}", reservation.getId()))
        .onErrorResume(e -> {
          log.error("Error processing release request: {}", e.getMessage());
          return Mono.just(false);
        });
  }

  @Override
  public Mono<Boolean> confirmInventory(Reservation reservation) {
    log.info("Confirming inventory for reservation {}", reservation.getId());
    // For confirmation, we'll just return success as the actual confirmation might
    // be handled by a different service
    return Mono.just(true)
        .doOnSuccess(v -> log.info("Confirmation processed for reservation {}", reservation.getId()))
        .onErrorResume(e -> {
          log.error("Error processing confirmation: {}", e.getMessage());
          return Mono.just(false);
        });
  }
}
