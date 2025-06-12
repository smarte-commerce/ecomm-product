package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.core.model.entity.Reservation;
import com.winnguyen1905.product.core.service.InventoryReservationService;
import com.winnguyen1905.product.core.service.InventoryService;
import com.winnguyen1905.product.core.service.InventoryValidationService;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.repository.ReservationRepository;
import com.winnguyen1905.product.exception.BadRequestException;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import com.winnguyen1905.product.config.ReservationExpiredEvent;
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
import reactor.util.retry.Retry;

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
  // Note: blockingRedisTemplate is kept for potential future use with blocking
  // operations
  @SuppressWarnings("unused")
  private final RedisTemplate<String, Object> blockingRedisTemplate;

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
    return Mono.fromCallable(() -> inventoryRepository.findBySku(sku)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + sku)))
        .flatMap(inventory -> {
          if (inventory.getQuantityAvailable() < quantity) {
            return Mono.error(new BadRequestException("Insufficient stock for SKU: " + sku +
                ". Available: " + inventory.getQuantityAvailable() +
                ", Requested: " + quantity));
          }
          return Mono.just(inventory);
        });
  }

  @Override
  @Transactional
  public Mono<Boolean> reserveInventory(String sku, int quantity) {
    final int maxRetries = 3;
    final int retryDelayMs = 100;
    final int[] attempt = { 0 };

    return validateAndGetInventory(sku, quantity)
        .flatMap(inventory -> {
          // Get the current version of the inventory
          long currentVersion = inventory.getVersion();

          // Check if we have enough stock
          if (inventory.getQuantityAvailable() < quantity) {
            log.warn("Insufficient stock for SKU: {}. Available: {}, Requested: {}",
                sku, inventory.getQuantityAvailable(), quantity);
            return Mono.just(false);
          }

          // Update the inventory with optimistic locking
          return Mono.fromCallable(() -> inventoryRepository.findBySkuWithLock(sku)
              .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku)))
              .flatMap(existingInventory -> {
                // Verify the version hasn't changed
                if (existingInventory.getVersion() != currentVersion) {
                  log.warn("Version conflict for SKU: {} (attempt {}/{}). Retrying...",
                      sku, attempt[0] + 1, maxRetries);

                  if (attempt[0]++ < maxRetries - 1) {
                    try {
                      Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                      Thread.currentThread().interrupt();
                      return Mono.error(ie);
                    }
                    return Mono.error(new RuntimeException("Retry"));
                  }
                  return Mono
                      .error(new RuntimeException("Failed to reserve inventory after " + maxRetries + " attempts"));
                }

                // Update quantities
                existingInventory.setQuantityAvailable(existingInventory.getQuantityAvailable() - quantity);
                existingInventory.setQuantityReserved(
                    (existingInventory.getQuantityReserved() != null ? existingInventory.getQuantityReserved() : 0)
                        + quantity);

                // Save with optimistic locking
                inventoryRepository.save(existingInventory);
                log.info("Successfully reserved {} items for SKU: {}", quantity, sku);
                return Mono.just(true);
              });
        })
        .retryWhen(Retry.fixedDelay(maxRetries, Duration.ofMillis(retryDelayMs))
            .filter(e -> e.getMessage() != null && e.getMessage().equals("Retry"))
            .onRetryExhaustedThrow((retryBackoffSpec,
                retrySignal) -> new RuntimeException("Failed to reserve inventory after " + maxRetries + " attempts")))
        .onErrorResume(e -> {
          log.error("Error reserving inventory for SKU: {}", sku, e);
          return Mono.just(false);
        });
  }

  @Override
  @Transactional
  public Mono<Boolean> releaseInventory(String sku, int quantity) {
    String lockKey = INVENTORY_LOCK_PREFIX + sku;
    String inventoryKey = INVENTORY_KEY_PREFIX + sku;

    return Mono.fromCallable(() -> inventoryRepository.findBySku(sku)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + sku)))
        .flatMap(inventory -> redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", Duration.ofSeconds(LOCK_TIMEOUT_SECONDS))
            .flatMap(locked -> {
              if (Boolean.TRUE.equals(locked)) {
                return redisTemplate.opsForValue()
                    .get(inventoryKey)
                    .defaultIfEmpty(String.valueOf(inventory.getQuantityAvailable()))
                    .flatMap(currentStock -> {
                      int available = Integer.parseInt(currentStock.toString());
                      return redisTemplate.opsForValue()
                          .set(inventoryKey, String.valueOf(available + quantity), Duration.ofDays(1))
                          .flatMap(result -> redisTemplate.delete(lockKey).thenReturn(true));
                    });
              }
              return Mono.just(false);
            }));
  }

  @Override
  @Transactional
  public Mono<Boolean> confirmReservation(String sku, int quantity) {
    return Mono.fromCallable(() -> {
      var inventory = inventoryRepository.findBySku(sku)
          .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + sku));

      if (inventory.getQuantityReserved() < quantity) {
        throw new BadRequestException("Insufficient reserved quantity for SKU: " + sku);
      }

      inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
      inventory.setQuantitySold(inventory.getQuantitySold() + quantity);
      inventoryRepository.save(inventory);
      return true;
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
        .results(inventories)
        .page(page.getNumber())
        .size(page.getSize())
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
    return Mono.fromCallable(() -> inventoryRepository.findBySku(sku)
        .map(inventory -> {
          inventory.setQuantityAvailable(quantityAvailable);
          inventory.setQuantityReserved(quantityReserved);
          inventory.setQuantitySold(quantitySold);
          return inventoryRepository.save(inventory) != null;
        })
        .orElse(false));
  }

  @Override
  @Transactional
  public Mono<InventoryVm> reserveInventory(UUID inventoryId, Integer quantity) {
    return Mono.fromCallable(() -> inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + inventoryId)))
        .flatMap(inventory -> {
          if (!isAccessStock(inventory, quantity)) {
            return Mono.error(new BadRequestException(
                "Insufficient available quantity for inventory ID: " + inventoryId));
          }
          inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
          inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
          return Mono.fromCallable(() -> inventoryRepository.save(inventory));
        })
        .flatMap(inventory -> {
          // Create a reservation object for the reservation operation
          Reservation.ReservationItem item = new Reservation.ReservationItem();
          item.setSku(inventory.getSku());
          item.setQuantity(quantity);

          Reservation reservation = new Reservation();
          reservation.setId(UUID.randomUUID());
          reservation.setItems(List.of(item));
          reservation.setStatus("PENDING");
          return inventoryReservationService.reserveInventory(reservation)
              .thenReturn(inventory);
        })
        .map(this::mapToInventoryVm);
  }

  @Override
  @Transactional
  public Mono<InventoryVm> releaseInventory(UUID inventoryId, Integer quantity) {
    return Mono.fromCallable(() -> inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + inventoryId)))
        .flatMap(inventory -> {
          if (inventory.getQuantityReserved() < quantity) {
            return Mono.error(new BadRequestException(
                "Insufficient reserved quantity for inventory ID: " + inventoryId));
          }
          inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
          inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
          return Mono.fromCallable(() -> inventoryRepository.save(inventory));
        })
        .flatMap(inventory -> {
          // Create a reservation object for the release operation
          Reservation.ReservationItem item = new Reservation.ReservationItem();
          item.setSku(inventory.getSku());
          item.setQuantity(quantity);

          Reservation releaseRequest = new Reservation();
          releaseRequest.setId(UUID.randomUUID());
          releaseRequest.setItems(List.of(item));
          releaseRequest.setStatus("RELEASED");

          return inventoryReservationService.releaseInventory(releaseRequest)
              .thenReturn(inventory);
        })
        .map(this::mapToInventoryVm);
  }

  @Override
  @Transactional
  public Mono<Boolean> releaseExpiredReservation(UUID reservationId) {
    log.info("Releasing inventory for expired reservation: {}", reservationId);

    return reservationRepository.findById(reservationId.toString())
        .switchIfEmpty(Mono.<Reservation>defer(() -> {
          log.warn("Reservation not found: {}", reservationId);
          return Mono.error(new RuntimeException("Reservation not found: " + reservationId));
        }))
        .flatMap(reservation -> {
          log.info("Processing release for expired reservation: {}", reservationId);

          // If reservation is already confirmed or cancelled, skip processing
          if ("CONFIRMED".equals(reservation.getStatus()) || "CANCELLED".equals(reservation.getStatus())) {
            log.info("Skipping release for reservation {} with status: {}",
                reservationId, reservation.getStatus());
            return Mono.just(true);
          }

          // Update reservation status to EXPIRED
          reservation.setStatus("EXPIRED");
          reservation.setUpdatedAt(Instant.now());

          // Process each item in the reservation
          return Flux.fromIterable(reservation.getItems())
              .flatMap(item -> {
                log.debug("Releasing {} units of SKU {} from reservation {}",
                    item.getQuantity(), item.getSku(), reservationId);

                // Find inventory by SKU and update atomically
                return Mono.fromCallable(() -> inventoryRepository.findBySku(item.getSku())
                    .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + item.getSku())))
                    .flatMap(inventory -> {
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

                      return Mono.fromCallable(() -> inventoryRepository.save(inventory));
                    });
              })
              .then(Mono.fromCallable(() -> {
                // Save the updated reservation with EXPIRED status
                log.debug("Saving reservation {} with EXPIRED status", reservationId);
                return reservationRepository.save(reservation);
              }))
              .thenReturn(true);
        })
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
