package com.winnguyen1905.product.core.mapper_v2;

import java.util.List;
import java.util.stream.Collectors;

import com.winnguyen1905.product.core.model.request.ProductInventoryDto;
import com.winnguyen1905.product.core.model.request.ReserveInventoryRequest;
import com.winnguyen1905.product.core.model.request.UpdateInventoryRequest;
import com.winnguyen1905.product.core.model.response.InventoryConfirmationResponse;
import com.winnguyen1905.product.core.model.response.ProductAvailabilityResponse;
import com.winnguyen1905.product.core.model.response.ReserveInventoryResponse;
import com.winnguyen1905.product.core.model.viewmodel.InventoryResponse;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.persistance.elasticsearch.ESInventory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.RInventory;
import com.winnguyen1905.product.core.model.response.InventoryDetailResponse;

public class InventoryMapper {

  /**
   * Map RInventory to Inventory model with enhanced fields
   */
  public static InventoryDetailResponse mapInventory(RInventory rInventory) {
    if (rInventory == null || rInventory.getInventory() == null) return null;

    EInventory eInventory = rInventory.getInventory();
    return InventoryDetailResponse.builder()
        .id(eInventory.getId())
        .sku(eInventory.getSku())
        .isDeleted(eInventory.getIsDeleted())
        .quantitySold(eInventory.getQuantitySold())
        .quantityReserved(eInventory.getQuantityReserved())
        .quantityAvailable(eInventory.getQuantityAvailable())
        .updatedDate(eInventory.getUpdatedDate() != null ?
            eInventory.getUpdatedDate().toString() : null)
        .build();
  }

  /**
   * Convert EInventory to InventoryVm for view models
   */
  public static InventoryVm toInventoryVm(EInventory inventory) {
    if (inventory == null) return null;

    return InventoryVm.builder()
        .id(inventory.getId())
        .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
        .sku(inventory.getSku())
        .quantityAvailable(inventory.getQuantityAvailable() != null ? inventory.getQuantityAvailable() : 0)
        .quantityReserved(inventory.getQuantityReserved() != null ? inventory.getQuantityReserved() : 0)
        .quantitySold(inventory.getQuantitySold() != null ? inventory.getQuantitySold() : 0)
        .address(inventory.getAddress())
        .build();
  }

  /**
   * Convert EInventory to InventoryResponse
   */
  public static InventoryResponse toInventoryResponse(EInventory inventory) {
    if (inventory == null) return null;

    return new InventoryResponse(
        inventory.getId(),
        inventory.getSku(),
        inventory.getQuantityAvailable(),
        inventory.getQuantityReserved(),
        inventory.getQuantitySold(),
        inventory.getProduct() != null ? inventory.getProduct().getId() : null,
        inventory.getVersion()
    );
  }

  /**
   * Convert EInventory to ESInventory for Elasticsearch
   */
  public static ESInventory toESInventory(EInventory inventory) {
    if (inventory == null) return null;

    return ESInventory.builder()
        .id(inventory.getId())
        .sku(inventory.getSku())
        .quantitySold(inventory.getQuantitySold())
        .quantityReserved(inventory.getQuantityReserved())
        .quantityAvailable(inventory.getQuantityAvailable())
        .build();
  }

  /**
   * Convert ProductInventoryDto to EInventory entity
   */
  public static EInventory toInventoryEntity(ProductInventoryDto inventory) {
    if (inventory == null) return null;

    return EInventory.builder()
        .sku(inventory.sku())
        .quantitySold(inventory.quantitySold())
        .quantityReserved(inventory.quantityReserved())
        .quantityAvailable(inventory.quantityAvailable())
        .build();
  }

  /**
   * Convert UpdateInventoryRequest to EInventory updates
   */
  public static void updateInventoryFromRequest(EInventory inventory, UpdateInventoryRequest request) {
    if (inventory == null || request == null) return;

    if (request.quantityAvailable() != null) {
      inventory.setQuantityAvailable(request.quantityAvailable());
    }
    if (request.quantityReserved() != null) {
      inventory.setQuantityReserved(request.quantityReserved());
    }
    if (request.quantitySold() != null) {
      inventory.setQuantitySold(request.quantitySold());
    }
  }

  /**
   * Convert list of EInventory to list of InventoryVm
   */
  public static List<InventoryVm> toInventoryVmList(List<EInventory> inventories) {
    if (inventories == null) return null;

    return inventories.stream()
        .map(InventoryMapper::toInventoryVm)
        .collect(Collectors.toList());
  }

  /**
   * Convert list of EInventory to list of InventoryResponse
   */
  public static List<InventoryResponse> toInventoryResponseList(List<EInventory> inventories) {
    if (inventories == null) return null;

    return inventories.stream()
        .map(InventoryMapper::toInventoryResponse)
        .collect(Collectors.toList());
  }

  /**
   * Create ProductAvailabilityResponse.Item from EInventory
   */
  public static ProductAvailabilityResponse.Item toAvailabilityItem(EInventory inventory) {
    if (inventory == null) {
      return ProductAvailabilityResponse.Item.builder()
          .available(false)
          .stockQuantity(0)
          .build();
    }

    boolean isAvailable = inventory.getQuantityAvailable() != null && inventory.getQuantityAvailable() > 0;

    return ProductAvailabilityResponse.Item.builder()
        .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
        .available(isAvailable)
        .isActive(true) // Assuming active if inventory exists
        .stockQuantity(inventory.getQuantityAvailable())
        .currentPrice(0.0) // Price should come from product variant
        .build();
  }

  /**
   * Create ReserveInventoryResponse from reservation result
   */
  public static ReserveInventoryResponse toReserveResponse(boolean success,
      ReserveInventoryRequest request, String message) {

    List<ReserveInventoryResponse.Item> responseItems = request.getItems().stream()
        .map(item -> ReserveInventoryResponse.Item.builder()
            .productId(item.getProductId())
            .variantId(item.getVariantId())
            .quantity(item.getQuantity())
            .build())
        .collect(Collectors.toList());

    return ReserveInventoryResponse.builder()
        .reservationId(request.getReservationId())
        .status(success)
        .items(responseItems)
        .expiresAt(request.getExpiresAt())
        .build();
  }

  /**
   * Create InventoryConfirmationResponse from confirmation result
   */
  public static InventoryConfirmationResponse toConfirmationResponse(
      java.util.UUID reservationId, java.util.UUID orderId,
      List<EInventory> inventories, boolean success) {

    List<InventoryConfirmationResponse.Item> responseItems = inventories.stream()
        .map(inventory -> InventoryConfirmationResponse.Item.builder()
            .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
            .quantityDeducted(0) // Should be calculated based on business logic
            .remainingStock(inventory.getQuantityAvailable())
            .build())
        .collect(Collectors.toList());

    InventoryConfirmationResponse response = new InventoryConfirmationResponse();
    response.setReservationId(reservationId);
    response.setOrderId(orderId);
    response.setItems(responseItems);

    return response;
  }
}
