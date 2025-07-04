package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.secure.RegionPartition;

import lombok.Builder;

@Builder
public record ProductResponse(
    UUID id,
    String name,
    String description,
    String shortDescription,
    String slug,
    ProductType productType,
    ProductStatus status,
    Boolean isPublished,
    UUID vendorId,
    UUID shopId,
    RegionPartition region,

    // Brand and Category
    BrandInfo brand,
    CategoryInfo category,

    // Pricing
    BigDecimal minPrice,
    BigDecimal maxPrice,
    BigDecimal basePrice,

    // Physical properties
    BigDecimal weight,
    BigDecimal length,
    BigDecimal width,
    BigDecimal height,

    // Features and specs
    Object features,
    Object specifications,

    // SEO fields
    String metaTitle,
    String metaDescription,
    String metaKeywords,
    String tags,

    // Settings
    Boolean trackInventory,
    Boolean allowBackorder,
    Integer lowStockThreshold,
    Boolean requiresShipping,

    // Analytics
    Long viewCount,
    Long purchaseCount,
    BigDecimal ratingAverage,
    Integer ratingCount,

    // Timestamps
    Instant createdDate,
    Instant updatedDate,
    String createdBy,
    String updatedBy,

    // Related data
    List<ProductVariantResponse> variants,
    List<ProductImageResponse> images,

    // Inventory summary
    InventorySummary inventory) {

  @Builder
  public record BrandInfo(
      UUID id,
      String name,
      String code,
      String logoUrl,
      Boolean isVerified) {
  }

  @Builder
  public record CategoryInfo(
      UUID id,
      String name,
      String code,
      String categoryPath,
      Integer categoryLevel) {
  }

  @Builder
  public record InventorySummary(
      Integer totalQuantity,
      Integer availableQuantity,
      Integer reservedQuantity,
      Integer soldQuantity,
      Boolean inStock) {
  }
}
