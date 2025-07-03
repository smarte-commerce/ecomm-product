package com.winnguyen1905.product.core.model.request;

import java.util.UUID;

import com.winnguyen1905.product.common.constant.ProductImageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateProductImageRequest(
    
    @NotBlank(message = "URL hình ảnh là bắt buộc")
    @Size(max = 1000, message = "URL hình ảnh không được vượt quá 1000 ký tự")
    String url,
    
    @Size(max = 255, message = "Alt text không được vượt quá 255 ký tự")
    String altText,
    
    @Size(max = 255, message = "Title không được vượt quá 255 ký tự")
    String title,
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    String description,
    
    @NotNull(message = "Loại hình ảnh là bắt buộc")
    ProductImageType type,
    
    Integer displayOrder,
    Boolean isPrimary,
    Boolean isActive,
    
    // Optional variant ID if this image is variant-specific
    UUID variantId,
    
    // Technical details
    String fileName,
    Long fileSize,
    String mimeType,
    Integer width,
    Integer height,
    
    // CDN URLs
    String cdnUrl,
    String thumbnailUrl,
    String smallUrl,
    String mediumUrl,
    String largeUrl,
    
    // Storage details
    String storageProvider,
    String storageBucket,
    String storageKey,
    
    // Metadata
    String colorPalette,
    Boolean isOptimized,
    Integer compressionQuality
) {
    
    public CreateProductImageRequest {
        if (displayOrder == null) displayOrder = 0;
        if (isPrimary == null) isPrimary = false;
        if (isActive == null) isActive = true;
        if (isOptimized == null) isOptimized = false;
        if (storageProvider == null) storageProvider = "S3";
    }
} 
