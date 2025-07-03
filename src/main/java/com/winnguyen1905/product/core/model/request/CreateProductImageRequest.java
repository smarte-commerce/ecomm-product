package com.winnguyen1905.product.core.model.request;

import java.util.UUID;

import com.winnguyen1905.product.common.constant.ProductImageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductImageRequest {
    private UUID productId;
    private UUID variantId;
    private ProductImageType type;
    private Boolean isPrimary;
    
    @Size(max = 255, message = "Title không được vượt quá 255 ký tự")
    private String title;
    
    @Size(max = 255, message = "Alt text không được vượt quá 255 ký tự")
    private String altText;
    
    @NotBlank(message = "URL hình ảnh là bắt buộc")
    @Size(max = 1000, message = "URL hình ảnh không được vượt quá 1000 ký tự")
    private String url;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
    
    private Integer displayOrder;
    private Boolean isActive;
    
    // Technical details
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
    
    // CDN URLs
    private String cdnUrl;
    private String thumbnailUrl;
    private String smallUrl;
    private String mediumUrl;
    private String largeUrl;
    
    // Storage details
    private String storageProvider;
    private String storageBucket;
    private String storageKey;
    
    // Metadata
    private String colorPalette;
    private Boolean isOptimized;
    private Integer compressionQuality;
} 
