package com.winnguyen1905.product.core.model.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateProductVariantRequest(
    
    @NotBlank(message = "SKU là bắt buộc")
    @Size(max = 100, message = "SKU không được vượt quá 100 ký tự")
    String sku,
    
    @Size(max = 255, message = "Tên variant không được vượt quá 255 ký tự")
    String name,
    
    @Size(max = 1000, message = "Mô tả variant không được vượt quá 1000 ký tự")
    String description,
    
    @NotNull(message = "Giá là bắt buộc")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    BigDecimal price,
    
    BigDecimal compareAtPrice,
    BigDecimal costPrice,
    
    // Physical properties
    BigDecimal weight,
    BigDecimal length,
    BigDecimal width,
    BigDecimal height,
    
    // Variant attributes (color, size, etc.)
    Object attributes,
    Object features,
    
    // Inventory settings
    Boolean trackInventory,
    Integer inventoryQuantity,
    Boolean isDefault,
    Boolean isActive
) {
    
    public CreateProductVariantRequest {
        if (trackInventory == null) trackInventory = true;
        if (inventoryQuantity == null) inventoryQuantity = 0;
        if (isDefault == null) isDefault = false;
        if (isActive == null) isActive = true;
    }
} 
