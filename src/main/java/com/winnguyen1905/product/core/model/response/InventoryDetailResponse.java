package com.winnguyen1905.product.core.model.response;

import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record InventoryDetailResponse(
    UUID id,
    String sku,
    String createdDate,
    String updatedDate,
    Boolean isDeleted,
    int quantitySold,
    int quantityReserved,
    int quantityAvailable) implements AbstractModel {
  @Builder
  public InventoryDetailResponse(
      UUID id,
      String sku,
      String createdDate,
      String updatedDate,
      Boolean isDeleted,
      int quantitySold,
      int quantityReserved,
      int quantityAvailable) {
    this.id = id;
    this.sku = sku;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.isDeleted = isDeleted;
    this.quantitySold = quantitySold;
    this.quantityReserved = quantityReserved;
    this.quantityAvailable = quantityAvailable;
  }
} 
