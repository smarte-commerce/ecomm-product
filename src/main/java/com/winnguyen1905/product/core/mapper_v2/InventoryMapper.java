package com.winnguyen1905.product.core.mapper_v2;

import com.winnguyen1905.product.core.model.Inventory;
import com.winnguyen1905.product.persistance.elasticsearch.ESInventory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.RInventory;

public class InventoryMapper {
  public static Inventory mapInventory(RInventory rInventory) {
    if (rInventory == null || rInventory.getInventory() == null) return null;
    
    EInventory eInventory = rInventory.getInventory();
    return Inventory.builder()
        .id(eInventory.getId())
        .createdDate(eInventory.getCreatedDate().toString())
        .updatedDate(eInventory.getUpdatedDate().toString())
        .isDeleted(eInventory.getIsDeleted())
        .quantitySold(eInventory.getQuantitySold())
        .quantityReserved(eInventory.getQuantityReserved())
        .quantityAvailable(eInventory.getQuantityAvailable())
        .build();
  }

  public static ESInventory toESInventory(EInventory inventory)  {
    if (inventory == null) return null;
    return ESInventory.builder()
        .id(inventory.getId())
        .quantitySold(inventory.getQuantitySold())
        .quantityReserved(inventory.getQuantityReserved())
        .quantityAvailable(inventory.getQuantityAvailable())
        .build();
  }

  public static EInventory toInventoryEntity(Inventory inventory) {
    if (inventory == null) return null;
    return EInventory.builder()
        .quantitySold(inventory.quantitySold())
        .quantityReserved(inventory.quantityReserved())
        .quantityAvailable(inventory.quantityAvailable())
        .build();
  }
}
