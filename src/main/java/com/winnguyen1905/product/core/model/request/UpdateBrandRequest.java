package com.winnguyen1905.product.core.model.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.UUID;

/**
 * Request DTO for updating an existing brand
 */
@Builder
public record UpdateBrandRequest(
    
    @Size(min = 2, max = 255, message = "Brand name must be between 2 and 255 characters")
    String name,
    
    @Size(min = 2, max = 100, message = "Brand code must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Brand code must contain only uppercase letters, numbers, hyphens, and underscores")
    String code,
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,
    
    @Size(max = 500, message = "Logo URL cannot exceed 500 characters")
    String logoUrl,
    
    @Size(max = 500, message = "Website URL cannot exceed 500 characters")
    String websiteUrl,
    
    Boolean isGlobalBrand,
    
    Boolean isActive,
    
    Boolean isVerified

) implements AbstractModel {

    /**
     * Constructor with validation and trimming
     */
    public UpdateBrandRequest {
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
        if (logoUrl != null) {
            logoUrl = logoUrl.trim();
        }
        if (websiteUrl != null) {
            websiteUrl = websiteUrl.trim();
        }
    }
    
    /**
     * Check if any field is provided for update
     */
    public boolean hasUpdates() {
        return name != null || code != null || description != null || 
               logoUrl != null || websiteUrl != null || isGlobalBrand != null || 
               isActive != null || isVerified != null;
    }
    
    /**
     * Get unique key for brand code validation
     */
    public String getUniqueKey(UUID vendorId) {
        return code != null ? code + "_" + vendorId : null;
    }
} 
