package com.winnguyen1905.product.core.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import lombok.Builder;

import java.util.UUID;

/**
 * Request DTO for creating a new brand
 */
@Builder
public record CreateBrandRequest(

    @NotBlank(message = "Brand name is required") @Size(min = 2, max = 255, message = "Brand name must be between 2 and 255 characters") String name,

    @NotBlank(message = "Brand code is required") @Size(min = 2, max = 100, message = "Brand code must be between 2 and 100 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Brand code must contain only uppercase letters, numbers, hyphens, and underscores") String code,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters") String description,

    @Size(max = 500, message = "Logo URL cannot exceed 500 characters") String logoUrl,

    @Size(max = 500, message = "Website URL cannot exceed 500 characters") String websiteUrl,

    Boolean isGlobalBrand,

    Boolean isActive

) implements AbstractModel {

  /**
   * Constructor with validation and defaults
   */
  public CreateBrandRequest {
    // Apply defaults if null
    if (isGlobalBrand == null) {
      isGlobalBrand = false;
    }
    if (isActive == null) {
      isActive = true;
    }

    // Trim strings
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
   * Validate that the brand code is unique within vendor scope
   */
  public String getUniqueKey(UUID vendorId) {
    return code + "_" + vendorId;
  }
}
