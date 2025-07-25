package com.winnguyen1905.product.util;

import com.winnguyen1905.product.exception.BadRequestException;
import com.winnguyen1905.product.exception.InsufficientInventoryException;
import com.winnguyen1905.product.persistance.entity.EInventory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class InventoryValidationUtils {

  /**
   * Validate if inventory has sufficient available stock
   * 
   * @param inventory The inventory entity
   * @param quantity  The quantity to check
   * @throws InsufficientInventoryException if insufficient stock
   */
  public static void validateSufficientStock(EInventory inventory, int quantity) {
    if (inventory.getQuantityAvailable() < quantity) {
      log.warn("Insufficient stock for SKU: {}, Available: {}, Requested: {}",
          inventory.getSku(), inventory.getQuantityAvailable(), quantity);
      throw new InsufficientInventoryException(
          "Insufficient stock available",
          inventory.getSku(),
          inventory.getQuantityAvailable(),
          quantity);
    }
  }

  /**
   * Validate if inventory has sufficient reserved stock
   * 
   * @param inventory The inventory entity
   * @param quantity  The quantity to check
   * @throws BadRequestException if insufficient reserved stock
   */
  public static void validateSufficientReservedStock(EInventory inventory, int quantity) {
    if (inventory.getQuantityReserved() < quantity) {
      log.warn("Insufficient reserved stock for SKU: {}, Reserved: {}, Requested: {}",
          inventory.getSku(), inventory.getQuantityReserved(), quantity);
      throw new BadRequestException(
          "Insufficient reserved quantity for SKU: " + inventory.getSku() +
              ". Requested: " + quantity + ", Available: " + inventory.getQuantityReserved());
    }
  }

  /**
   * Validate quantity is positive
   * 
   * @param quantity The quantity to check
   * @throws BadRequestException if quantity is not positive
   */
  public static void validatePositiveQuantity(int quantity) {
    if (quantity <= 0) {
      throw new BadRequestException("Quantity must be positive, got: " + quantity);
    }
  }

  /**
   * Validate SKU is not null or empty
   * 
   * @param sku The SKU to check
   * @throws BadRequestException if SKU is null or empty
   */
  public static void validateSku(String sku) {
    if (sku == null || sku.trim().isEmpty()) {
      throw new BadRequestException("SKU cannot be null or empty");
    }
  }

  /**
   * Validate inventory entity is not null
   * 
   * @param inventory The inventory entity to check
   * @throws BadRequestException if inventory is null
   */
  public static void validateInventoryNotNull(EInventory inventory) {
    if (inventory == null) {
      throw new BadRequestException("Inventory cannot be null");
    }
  }

  /**
   * Safely calculate new available quantity after reservation
   * 
   * @param currentAvailable Current available quantity
   * @param quantity         Quantity to reserve
   * @return New available quantity
   */
  public static int calculateNewAvailableAfterReservation(int currentAvailable, int quantity) {
    int newAvailable = currentAvailable - quantity;
    if (newAvailable < 0) {
      log.warn("Calculated negative available quantity: {} - {} = {}",
          currentAvailable, quantity, newAvailable);
      return 0;
    }
    return newAvailable;
  }

  /**
   * Safely calculate new reserved quantity after reservation
   * 
   * @param currentReserved Current reserved quantity (may be null)
   * @param quantity        Quantity to reserve
   * @return New reserved quantity
   */
  public static int calculateNewReservedAfterReservation(Integer currentReserved, int quantity) {
    return (currentReserved != null ? currentReserved : 0) + quantity;
  }

  /**
   * Safely calculate new available quantity after release
   * 
   * @param currentAvailable Current available quantity
   * @param quantity         Quantity to release
   * @return New available quantity
   */
  public static int calculateNewAvailableAfterRelease(int currentAvailable, int quantity) {
    return currentAvailable + quantity;
  }

  /**
   * Safely calculate new reserved quantity after release
   * 
   * @param currentReserved Current reserved quantity
   * @param quantity        Quantity to release
   * @return New reserved quantity
   */
  public static int calculateNewReservedAfterRelease(int currentReserved, int quantity) {
    int newReserved = currentReserved - quantity;
    if (newReserved < 0) {
      log.warn("Calculated negative reserved quantity: {} - {} = {}, setting to 0",
          currentReserved, quantity, newReserved);
      return 0;
    }
    return newReserved;
  }

  /**
   * Calculate quantity that can actually be released (to handle cases where
   * requested > reserved)
   * 
   * @param currentReserved   Current reserved quantity
   * @param requestedQuantity Requested quantity to release
   * @return Actual quantity that can be released
   */
  public static int calculateActualReleaseQuantity(int currentReserved, int requestedQuantity) {
    return Math.min(currentReserved, requestedQuantity);
  }
}
