package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.core.model.request.AbstractModel;
import com.winnguyen1905.product.core.model.request.ProductImageRequest;

import lombok.Builder;

@Builder
public record ProductVariantDetailVm(
    UUID id,
    int stock,
    String sku,
    double price,
    UUID productId,
    JsonNode features,
    List<ProductImageRequest> images) implements AbstractModel {
  @Builder
  public ProductVariantDetailVm(
      UUID id,
      int stock,
      String sku,
      double price,
      UUID productId,
      JsonNode features,
      List<ProductImageRequest> images) {
    this.id = id;
    this.stock = stock;
    this.sku = sku;
    this.price = price;
    this.productId = productId;
    this.features = features;
    this.images = images;
  }
}
