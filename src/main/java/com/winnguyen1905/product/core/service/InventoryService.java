package com.winnguyen1905.product.core.service;

import reactor.core.publisher.Mono;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.persistance.entity.EInventory;

public interface InventoryService {
  public Boolean handleUpdateInventoryForReservation(UUID inventoryId, UUID customerId, Integer quantity);
    // Inventory management methods
    PagedResponse<InventoryVm> getProductInventory(UUID productId, Pageable pageable);
    InventoryVm getInventoryById(UUID inventoryId);
    InventoryVm getInventoryBySku(String sku);
    InventoryVm updateInventory(UUID inventoryId);
    
    // Reactive reservation methods
    Mono<InventoryVm> reserveInventory(UUID inventoryId, Integer quantity);
    Mono<InventoryVm> releaseInventory(UUID inventoryId, Integer quantity);
    Mono<Boolean> reserveInventory(String sku, int quantity);
    Mono<Boolean> releaseInventory(String sku, int quantity);
    Mono<Boolean> confirmReservation(String sku, int quantity);
    
    Mono<EInventory> validateAndGetInventory(String sku, int quantity);
    
    // New methods for reservation management
    Mono<Boolean> hasSufficientStock(String sku, int quantity);
    Mono<Boolean> updateStock(String sku, int quantityAvailable, int quantityReserved, int quantitySold);

    Mono<Boolean> releaseExpiredReservation(UUID reservationId);
}
