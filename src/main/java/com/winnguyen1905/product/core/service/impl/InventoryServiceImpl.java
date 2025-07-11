package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.core.model.entity.Reservation;
import com.winnguyen1905.product.core.service.InventoryReservationService;
import com.winnguyen1905.product.core.service.InventoryService;
import com.winnguyen1905.product.core.service.InventoryValidationService;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.exception.BadRequestException;
import com.winnguyen1905.product.exception.InsufficientInventoryException;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import com.winnguyen1905.product.persistance.repository.ReservationRepository;
import com.winnguyen1905.product.config.ReservationExpiredEvent;
import com.winnguyen1905.product.util.InventoryLockingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService, InventoryValidationService {

  private static final String INVENTORY_KEY_PREFIX = "inventory:";
  private static final String INVENTORY_LOCK_PREFIX = "inventory:lock:";
  private static final long LOCK_TIMEOUT_SECONDS = 30;

  private final InventoryRepository inventoryRepository;
  private final ReservationRepository reservationRepository;
  private final InventoryReservationService inventoryReservationService;
  private final ReactiveRedisTemplate<String, Object> redisTemplate;
  // Note: blockingRedisTemplate is kept for potential future use with blocking operations
  @SuppressWarnings("unused")
  private final RedisTemplate<String, Object> blockingRedisTemplate;
  private final InventoryLockingUtils inventoryLockingUtils;

  @Transactional(readOnly = true)
  public Boolean isAccessStock(EInventory inventory, Integer quantity) {
    return inventory.getQuantityAvailable() >= quantity;
  }

  @Override
  @Transactional
  public Boolean handleUpdateInventoryForReservation(UUID inventoryId, UUID customerId, Integer quantity) {
    return inventoryRepository.findById(inventoryId)
        .map(inventory -> {
          if (inventory.getQuantityAvailable() < quantity) {
            return false;
          }
          inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
          inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
          inventoryRepository.save(inventory);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public Mono<EInventory> validateAndGetInventory(String sku, int quantity) {
    return Mono.fromCallable(() -> inventoryRepository.findBySkuWithOptimisticLock(sku)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + sku)))
        .flatMap(inventory -> {
            log.debug("Validating inventory for SKU: {}, requested: {}, available: {}", 
                sku, quantity, inventory.getQuantityAvailable());
                
            if (inventory.getQuantityAvailable() < quantity) {
                return Mono.error(new InsufficientInventoryException(
                    "Insufficient stock", 
                    sku, 
                    inventory.getQuantityAvailable(), 
                    quantity));
            }
            
            return Mono.just(inventory);
        })
        .onErrorResume(e -> {
            if (e instanceof ResourceNotFoundException) {
                log.warn("Inventory not found for SKU: {}", sku);
            } else if (e instanceof BadRequestException) {
                log.warn("Insufficient stock for SKU: {}: {}", sku, e.getMessage());
            } else {
                log.error("Error validating inventory for SKU: {}", sku, e);
            }
            return Mono.error(e);
        });
  }

  @Override
  @Transactional
  public Mono<Boolean> reserveInventory(String sku, int quantity) {
    log.debug("Attempting to reserve {} items for SKU: {}", quantity, sku);
    
    return inventoryLockingUtils.executeWithOptimisticLockBySkuReturningBoolean(sku, inventory -> {
        if (inventory.getQuantityAvailable() < quantity) {
            throw new InsufficientInventoryException(
                "Insufficient stock", 
                sku, 
                inventory.getQuantityAvailable(), 
                quantity);
        }
        
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
        inventory.setQuantityReserved(
            (inventory.getQuantityReserved() != null ? inventory.getQuantityReserved() : 0) + quantity);
        
        log.info("Successfully reserved {} items for SKU: {}", quantity, sku);
        return inventory;
    })
    .onErrorResume(e -> {
        if (e instanceof InsufficientInventoryException) {
            log.warn("Insufficient stock when reserving inventory for SKU: {}: {}", sku, e.getMessage());
            return Mono.just(false);
        }
        log.error("Unexpected error reserving inventory for SKU: {}", sku, e);
        return Mono.just(false);
    });
  }

  @Override
  @Transactional
  public Mono<Boolean> releaseInventory(String sku, int quantity) {
    log.debug("Attempting to release {} items for SKU: {}", quantity, sku);
    
    return inventoryLockingUtils.executeWithOptimisticLockBySkuReturningBoolean(sku, inventory -> {
        // Validate reserved quantity
        if (inventory.getQuantityReserved() < quantity) {
            throw new BadRequestException(
                "Insufficient reserved quantity for SKU: " + sku + 
                ". Requested: " + quantity + ", Available: " + inventory.getQuantityReserved());
        }
        
        // Update inventory counts
        inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
        
        log.info("Successfully released {} items for SKU: {}", quantity, sku);
        return inventory;
    })
    .flatMap(success -> {
        // Update cache as well
        String inventoryKey = INVENTORY_KEY_PREFIX + sku;
        return redisTemplate.opsForValue()
            .set(inventoryKey, String.valueOf(quantity), Duration.ofDays(1))
            .thenReturn(success);
    })
    .onErrorResume(e -> {
        if (e instanceof BadRequestException) {
            log.warn("Invalid request when releasing inventory for SKU: {}: {}", sku, e.getMessage());
            return Mono.just(false);
        }
        log.error("Unexpected error releasing inventory for SKU: {}", sku, e);
        return Mono.just(false);
    });
  }

  @Override
  @Transactional
  public Mono<Boolean> confirmReservation(String sku, int quantity) {
    return inventoryLockingUtils.executeWithOptimisticLockBySkuReturningBoolean(sku, inventory -> {
        if (inventory.getQuantityReserved() < quantity) {
            throw new BadRequestException("Insufficient reserved quantity for SKU: " + sku);
        }

        inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
        inventory.setQuantitySold(inventory.getQuantitySold() + quantity);
        return inventory;
    })
    .onErrorResume(e -> {
        log.error("Error confirming reservation for SKU: {}", sku, e);
        return Mono.just(false);
    });
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<InventoryVm> getProductInventory(UUID productId, Pageable pageable) {
    Page<EInventory> page = inventoryRepository.findByProductId(productId, pageable);
    List<InventoryVm> inventories = page.getContent().stream()
        .map(this::mapToInventoryVm)
        .collect(Collectors.toList());

    return PagedResponse.<InventoryVm>builder()
        .content(inventories) 
        .pageNumber(page.getNumber())
        .pageSize(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public InventoryVm getInventoryById(UUID inventoryId) {
    return inventoryRepository.findById(inventoryId)
        .map(this::mapToInventoryVm)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + inventoryId));
  }

  @Override
  @Transactional(readOnly = true)
  public InventoryVm getInventoryBySku(String sku) {
    return inventoryRepository.findBySku(sku)
        .map(this::mapToInventoryVm)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with SKU: " + sku));
  }

  @Override
  @Transactional
  public InventoryVm updateInventory(UUID inventoryId) {
    EInventory inventory = inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + inventoryId));
    return mapToInventoryVm(inventoryRepository.save(inventory));
  }

  @Override
  @Transactional(readOnly = true)
  public Mono<Boolean> hasSufficientStock(String sku, int quantity) {
    return Mono.fromCallable(() -> inventoryRepository.findBySku(sku)
        .map(inv -> inv.getQuantityAvailable() >= quantity)
        .orElse(false));
  }

  @Override
  @Transactional
  public Mono<Boolean> updateStock(String sku, int quantityAvailable, int quantityReserved, int quantitySold) {
    return inventoryLockingUtils.executeWithOptimisticLockBySkuReturningBoolean(sku, inventory -> {
        inventory.setQuantityAvailable(quantityAvailable);
        inventory.setQuantityReserved(quantityReserved);
        inventory.setQuantitySold(quantitySold);
        return inventory;
    });
  }

  @Override
  @Transactional
  public Mono<InventoryVm> reserveInventory(UUID inventoryId, Integer quantity) {
    return inventoryLockingUtils.executeWithOptimisticLock(inventoryId, inventory -> {
        if (!isAccessStock(inventory, quantity)) {
            throw new InsufficientInventoryException(
                "Insufficient available quantity", 
                inventory.getSku(), 
                inventory.getQuantityAvailable(), 
                quantity);
        }
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
        return inventory;
    })
    .map(this::mapToInventoryVm);
  }

  @Override
  @Transactional
  public Mono<InventoryVm> releaseInventory(UUID inventoryId, Integer quantity) {
    return inventoryLockingUtils.executeWithOptimisticLock(inventoryId, inventory -> {
        if (inventory.getQuantityReserved() < quantity) {
            throw new BadRequestException("Insufficient reserved quantity for inventory ID: " + inventoryId);
        }
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
        inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
        return inventory;
    })
    .map(this::mapToInventoryVm);
  }

  @Override
  @Transactional
  public Mono<Boolean> releaseExpiredReservation(UUID reservationId) {
    log.info("Releasing inventory for expired reservation: {}", reservationId);

    return findAndValidateReservation(reservationId)
        .flatMap(this::processReservationRelease)
        .onErrorResume(e -> {
            log.error("Error releasing inventory for reservation {}: {}",
                reservationId, e.getMessage(), e);
            return Mono.just(false);
        })
        .doOnSuccess(success -> {
            if (success) {
                log.info("Successfully released inventory for reservation: {}", reservationId);
            } else {
                log.warn("Failed to release inventory for reservation: {}", reservationId);
            }
        });
  }

  private Mono<Reservation> findAndValidateReservation(UUID reservationId) {
    return reservationRepository.findById(reservationId.toString())
        .switchIfEmpty(Mono.<Reservation>defer(() -> {
            log.warn("Reservation not found: {}", reservationId);
            return Mono.error(new ResourceNotFoundException("Reservation not found: " + reservationId));
        }))
        .flatMap(reservation -> {
            log.info("Processing release for expired reservation: {}", reservationId);

            // If reservation is already confirmed or cancelled, skip processing
            if ("CONFIRMED".equals(reservation.getStatus()) || 
                "CANCELLED".equals(reservation.getStatus()) ||
                "EXPIRED".equals(reservation.getStatus())) {
                log.info("Skipping release for reservation {} with status: {}",
                    reservationId, reservation.getStatus());
                return Mono.error(new IllegalStateException(
                    "Reservation " + reservationId + " has status " + reservation.getStatus() + 
                    " and cannot be expired"));
            }
            
            // Update reservation status to EXPIRED
            reservation.setStatus("EXPIRED");
            reservation.setUpdatedAt(Instant.now());
            
            return reservationRepository.save(reservation)
                .doOnSuccess(saved -> log.debug("Updated reservation {} status to EXPIRED", reservationId));
        });
  }

  private Mono<Boolean> processReservationRelease(Reservation reservation) {
    // Process each item in the reservation
    return Flux.fromIterable(reservation.getItems())
        .flatMap(item -> releaseReservationItem(item, reservation.getId()))
        .then(Mono.just(true))
        .onErrorResume(e -> {
            log.error("Error processing reservation release: {}", e.getMessage(), e);
            return Mono.just(false);
        });
  }

  private Mono<Boolean> releaseReservationItem(Reservation.ReservationItem item, UUID reservationId) {
    log.debug("Releasing {} units of SKU {} from reservation {}",
        item.getQuantity(), item.getSku(), reservationId);

    return inventoryLockingUtils.executeWithOptimisticLockBySkuReturningBoolean(item.getSku(), inventory -> {
        // Update inventory quantities
        int newReserved = Math.max(0, inventory.getQuantityReserved() - item.getQuantity());
        int releasedQuantity = inventory.getQuantityReserved() - newReserved;
        int newAvailable = inventory.getQuantityAvailable() + releasedQuantity;

        if (releasedQuantity < item.getQuantity()) {
            log.warn("Insufficient reserved quantity for SKU: {}. Current reserved: {}, " +
                "attempting to release: {}, actually released: {}",
                item.getSku(), inventory.getQuantityReserved(),
                item.getQuantity(), releasedQuantity);
        }

        inventory.setQuantityReserved(newReserved);
        inventory.setQuantityAvailable(newAvailable);
        inventory.setUpdatedDate(Instant.now());
        
        log.debug("Successfully released {} units for SKU: {}", releasedQuantity, item.getSku());
        return inventory;
    })
    .onErrorResume(e -> {
        if (e instanceof ResourceNotFoundException) {
            log.warn("Inventory not found for SKU: {}", item.getSku());
            return Mono.just(false);
        }
        log.error("Error releasing inventory for SKU {}: {}", item.getSku(), e.getMessage(), e);
        return Mono.just(false);
    });
  }

  @Async
  @EventListener
  public void handleReservationExpiredEvent(ReservationExpiredEvent event) {
    log.info("Handling expired reservation event: {}", event.getReservationId());
    releaseExpiredReservation(event.getReservationId())
        .subscribe(
            result -> log.info("Successfully released inventory for expired reservation: {}", event.getReservationId()),
            error -> log.error("Failed to release inventory for expired reservation {}: {}",
                event.getReservationId(), error.getMessage()));
  }

  private InventoryVm mapToInventoryVm(EInventory inventory) {
    if (inventory == null) {
      return null;
    }
    return InventoryVm.builder()
        .id(inventory.getId())
        .sku(inventory.getSku())
        .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
        .quantityAvailable(inventory.getQuantityAvailable())
        .quantityReserved(inventory.getQuantityReserved())
        .quantitySold(inventory.getQuantitySold())
        .address(inventory.getAddress())
        .build();
  }
}
