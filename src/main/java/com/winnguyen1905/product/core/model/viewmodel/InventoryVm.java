package com.winnguyen1905.product.core.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record InventoryVm(
    UUID id,
    UUID productId,
    String sku,
    int quantityAvailable,
    int quantityReserved,
    int quantitySold,
    String address) implements AbstractModel {
  @Builder
  public InventoryVm(
      UUID id,
      UUID productId,
      String sku,
      int quantityAvailable,
      int quantityReserved,
      int quantitySold,
      String address) {
    this.id = id;
    this.productId = productId;
    this.sku = sku;
    this.quantityAvailable = quantityAvailable;
    this.quantityReserved = quantityReserved;
    this.quantitySold = quantitySold;
    this.address = address;
  }
}
