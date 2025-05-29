package com.winnguyen1905.product.core.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record ProductVariantReviewVm(
    UUID id,
    int stock,
    String name,
    String sku,
    double price,   
    UUID productId,
    Object features, String imageUrl) implements AbstractModel {
  @Builder
  public ProductVariantReviewVm(
      UUID id,
      int stock,
      String name,
      String sku,
      double price,
      UUID productId,
      Object features, String imageUrl) {
    this.id = id;
    this.stock = stock;
    this.name = name;
    this.sku = sku;
    this.price = price;
    this.productId = productId;
    this.features = features;
    this.imageUrl = imageUrl;
  }
}
