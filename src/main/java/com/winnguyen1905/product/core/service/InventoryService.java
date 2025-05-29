package com.winnguyen1905.product.core.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.persistance.entity.EInventory;

public interface InventoryService {
  PagedResponse<InventoryVm> getProductInventory(UUID productId, Pageable pageable);
  InventoryVm getInventoryById(UUID inventoryId);
  InventoryVm getInventoryBySku(String sku);
  InventoryVm updateInventory(UUID inventoryId);
  InventoryVm reserveInventory(UUID inventoryId, Integer quantity);
  InventoryVm releaseInventory(UUID inventoryId, Integer quantity);
  Boolean isAccessStock(EInventory inventory, Integer quantity);
  // Boolean handleUpdateInventoryForReservation(UUID inventory, UUID customerId, Integer quantity);
}
