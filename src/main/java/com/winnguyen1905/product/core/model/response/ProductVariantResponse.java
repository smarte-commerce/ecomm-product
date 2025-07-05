package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.secure.RegionPartition;

import lombok.Builder;

@Builder
public record ProductVariantResponse(
    UUID id,
    String sku,
    String name,
    String description,
    
    // Pricing
    BigDecimal price,
    BigDecimal compareAtPrice,
    BigDecimal costPrice,
    
    // Status
    Boolean isActive,
    Boolean isDefault,
    
    // Physical properties
    BigDecimal weight,
    BigDecimal length,
    BigDecimal width,
    BigDecimal height,
    
    // Attributes and features
    Object attributes,
    Object features,
    
    // Inventory
    Boolean trackInventory,
    Integer inventoryQuantity,
    Integer reservedQuantity,
    Integer availableQuantity,
    
    // Analytics
    Long viewCount,
    Long purchaseCount,
    
    // Vendor info
    UUID vendorId,
    RegionPartition region,
    
    // Timestamps
    Instant createdDate,
    Instant updatedDate,
    String createdBy,
    String updatedBy,
    
    // Related data
    UUID productId,
    String productName,
    List<ProductImageResponse> images,
    
    // Calculated fields
    Boolean hasDiscount,
    BigDecimal discountPercentage,
    BigDecimal profitMargin,
    Boolean isAvailable,
    Boolean inStock
) {} 
