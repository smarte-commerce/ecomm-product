package com.winnguyen1905.product.core.model.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.common.constant.ProductStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateProductRequest(
    UUID id,
    
    @Size(min = 2, max = 255, message = "Tên sản phẩm phải từ 2-255 ký tự")
    String name,
    
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    String description,
    
    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự")
    String shortDescription,
    
    @Size(max = 300, message = "Slug không được vượt quá 300 ký tự")
    String slug,
    
    // Product categorization
    UUID brandId,
    UUID categoryId,
    String categoryCode,
    String brandCode,
    
    // Pricing
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá cơ bản phải lớn hơn 0")
    BigDecimal basePrice,
    
    // Product features and specifications
    JsonNode features,
    Object specifications,
    
    // Physical properties
    BigDecimal weight,
    BigDecimal length,
    BigDecimal width,
    BigDecimal height,
    
    // Inventory settings
    Boolean trackInventory,
    Boolean allowBackorder,
    Integer lowStockThreshold,
    Boolean requiresShipping,
    
    // SEO fields
    String metaTitle,
    String metaDescription,
    String metaKeywords,
    String tags,
    
    // Status
    ProductStatus status,
    Boolean isPublished,
    
    // Variants and images (for complex updates)
    List<UpdateProductVariantRequest> variants,
    List<UpdateProductImageRequest> images
) implements AbstractModel {

    // Nested request classes for complex updates
    @Builder
    public static record UpdateProductVariantRequest(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        BigDecimal compareAtPrice,
        BigDecimal costPrice,
        Boolean isActive,
        Boolean isDefault,
        BigDecimal weight,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        Object attributes,
        Object features,
        Boolean trackInventory,
        Integer inventoryQuantity
    ) {}
    
    @Builder
    public static record UpdateProductImageRequest(
        UUID id,
        String url,
        String altText,
        String title,
        String description,
        Integer displayOrder,
        Boolean isPrimary,
        Boolean isActive,
        UUID variantId
    ) {}
}
