package com.winnguyen1905.product.core.service;

import java.util.UUID;

import com.winnguyen1905.product.persistance.entity.EInventory;

public interface InventoryService {
  Boolean isAccessStock(EInventory inventory, Integer quantity);

  Boolean handleUpdateInventoryForReservation(UUID inventory, UUID customerId, Integer quantity);
}
