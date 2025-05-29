package com.winnguyen1905.product.core.model.viewmodel;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.AbstractModel;
import com.winnguyen1905.product.core.model.request.ProductImageRequest;

import lombok.Builder;

@Builder
public record ProductDetailVm(
    UUID id,
    String name,
    String slug,
    String brand,
    Double price,
    String category,
    JsonNode features,
    Boolean isDeleted,
    String updatedDate,
    String productType,
    String description,
    String createdDate,
    List<ProductImageRequest> images,
    List<ProductVariantDetailVm> variations) implements AbstractModel {

  @Builder
  public ProductDetailVm(
      UUID id,
      String name,
      String slug,
      String brand,
      Double price,
      String category,
      JsonNode features,
      Boolean isDeleted,
      String updatedDate,
      String productType,
      String description,
      String createdDate,
      List<ProductImageRequest> images,
      List<ProductVariantDetailVm> variations) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.brand = brand;
    this.price = price;
    this.category = category;
    this.features = features;
    this.isDeleted = isDeleted;
    this.updatedDate = updatedDate;
    this.productType = productType;
    this.description = description;
    this.createdDate = createdDate;
    this.images = images;
    this.variations = variations;
  }
}
