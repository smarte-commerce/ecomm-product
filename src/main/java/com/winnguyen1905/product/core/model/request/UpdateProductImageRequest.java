package com.winnguyen1905.product.core.model.request;

import com.winnguyen1905.product.common.constant.ProductImageType;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.UUID;

/**
 * Request DTO for updating product image metadata
 */
@Builder
public record UpdateProductImageRequest(
    
    @Size(max = 255, message = "Image title cannot exceed 255 characters")
    String title,
    
    @Size(max = 255, message = "Alt text cannot exceed 255 characters")
    String altText,
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,
    
    ProductImageType type,
    
    UUID variantId,
    
    Boolean isPrimary,
    
    Boolean isActive,
    
    @Min(value = 0, message = "Display order must be non-negative")
    Integer displayOrder,
    
    @Size(max = 500, message = "Color palette cannot exceed 500 characters")
    String colorPalette,
    
    Boolean isOptimized,
    
    @Min(value = 1, message = "Compression quality must be at least 1")
    Integer compressionQuality

) implements AbstractModel {

    /**
     * Constructor with validation and trimming
     */
    public UpdateProductImageRequest {
        // Trim strings if provided
        if (title != null) {
            title = title.trim();
        }
        if (altText != null) {
            altText = altText.trim();
        }
        if (description != null) {
            description = description.trim();
        }
        if (colorPalette != null) {
            colorPalette = colorPalette.trim();
        }
        
        // Validate compression quality range
        if (compressionQuality != null && compressionQuality > 100) {
            throw new IllegalArgumentException("Compression quality cannot exceed 100");
        }
    }
    
    /**
     * Check if any field is provided for update
     */
    public boolean hasUpdates() {
        return title != null || altText != null || description != null || 
               type != null || variantId != null || isPrimary != null || 
               isActive != null || displayOrder != null || colorPalette != null ||
               isOptimized != null || compressionQuality != null;
    }
    
    /**
     * Check if this update affects the image ordering
     */
    public boolean affectsOrdering() {
        return displayOrder != null || isPrimary != null;
    }
    
    /**
     * Check if this update affects image visibility
     */
    public boolean affectsVisibility() {
        return isActive != null || isPrimary != null;
    }
} 
