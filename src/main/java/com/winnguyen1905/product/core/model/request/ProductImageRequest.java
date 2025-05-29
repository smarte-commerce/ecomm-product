package com.winnguyen1905.product.core.model.request;

import com.winnguyen1905.product.common.constant.ProductImageType;

import lombok.Builder;

import java.util.UUID;

public record ProductImageRequest(
    ProductImageType type,
    String url,
    UUID productVariantId) {
  @Builder
  public ProductImageRequest(
      ProductImageType type,
      String url,
      UUID productVariantId) {
    this.type = type;
    this.url = url;
    this.productVariantId = productVariantId;
  }
}
