package com.winnguyen1905.product.core.model.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.UUID;

/**
 * Request DTO for updating an existing category
 */
@Builder
public record UpdateCategoryRequest(
    
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    String name,
    
    @Size(min = 2, max = 100, message = "Category code must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Category code must contain only uppercase letters, numbers, hyphens, and underscores")
    String code,
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,
    
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    String imageUrl,
    
    @Size(max = 500, message = "Icon URL cannot exceed 500 characters")
    String iconUrl,
    
    Boolean isPublished,
    
    Boolean isFeatured,
    
    Boolean isGlobalCategory,
    
    @Min(value = 0, message = "Display order must be non-negative")
    Integer displayOrder,
    
    @Size(max = 255, message = "Meta title cannot exceed 255 characters")
    String metaTitle,
    
    @Size(max = 500, message = "Meta description cannot exceed 500 characters")
    String metaDescription,
    
    @Size(max = 500, message = "Meta keywords cannot exceed 500 characters")
    String metaKeywords,
    
    @Size(max = 255, message = "Slug cannot exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    String slug,
    
    Object productFeaturesTemplate

) implements AbstractModel {

    /**
     * Constructor with validation and trimming
     */
    public UpdateCategoryRequest {
        // Trim strings if provided
        if (name != null) {
            name = name.trim();
        }
        if (code != null) {
            code = code.trim().toUpperCase();
        }
        if (description != null) {
            description = description.trim();
        }
        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
        }
        if (iconUrl != null) {
            iconUrl = iconUrl.trim();
        }
        if (metaTitle != null) {
            metaTitle = metaTitle.trim();
        }
        if (metaDescription != null) {
            metaDescription = metaDescription.trim();
        }
        if (metaKeywords != null) {
            metaKeywords = metaKeywords.trim();
        }
        if (slug != null) {
            slug = slug.trim().toLowerCase();
        }
    }
    
    /**
     * Check if any field is provided for update
     */
    public boolean hasUpdates() {
        return name != null || code != null || description != null || 
               imageUrl != null || iconUrl != null || isPublished != null || 
               isFeatured != null || isGlobalCategory != null || displayOrder != null ||
               metaTitle != null || metaDescription != null || metaKeywords != null ||
               slug != null || productFeaturesTemplate != null;
    }
    
    /**
     * Get unique key for category code validation
     */
    public String getUniqueKey(UUID vendorId) {
        return code != null ? code + "_" + vendorId : null;
    }
    
    /**
     * Generate slug from name if updating name but no slug provided
     */
    public String generateSlugFromName() {
        if (name == null) {
            return null;
        }
        
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
} 
