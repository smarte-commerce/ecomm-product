package com.winnguyen1905.product.core.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.UUID;

/**
 * Request DTO for creating a new category
 */
@Builder
public record CreateCategoryRequest(

    @NotBlank(message = "Category name is required") @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters") String name,

    @NotBlank(message = "Category code is required") @Size(min = 2, max = 100, message = "Category code must be between 2 and 100 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Category code must contain only uppercase letters, numbers, hyphens, and underscores") String code,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters") String description,

    @Size(max = 500, message = "Image URL cannot exceed 500 characters") String imageUrl,

    @Size(max = 500, message = "Icon URL cannot exceed 500 characters") String iconUrl,

    UUID parentId,

    Boolean isPublished,

    Boolean isFeatured,

    Boolean isGlobalCategory,

    @Min(value = 0, message = "Display order must be non-negative") Integer displayOrder,

    @Size(max = 255, message = "Meta title cannot exceed 255 characters") String metaTitle,

    @Size(max = 500, message = "Meta description cannot exceed 500 characters") String metaDescription,

    @Size(max = 500, message = "Meta keywords cannot exceed 500 characters") String metaKeywords,

    @Size(max = 255, message = "Slug cannot exceed 255 characters") @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug must contain only lowercase letters, numbers, and hyphens") String slug,

    Object productFeaturesTemplate

) implements AbstractModel {

  /**
   * Constructor with validation and defaults
   */
  public CreateCategoryRequest {
    // Apply defaults if null
    if (isPublished == null) {
      isPublished = true;
    }
    if (isFeatured == null) {
      isFeatured = false;
    }
    if (isGlobalCategory == null) {
      isGlobalCategory = false;
    }
    if (displayOrder == null) {
      displayOrder = 0;
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
   * Generate slug from name if not provided
   */
  public String getSlugOrGenerate() {
    if (slug != null && !slug.trim().isEmpty()) {
      return slug;
    }
    return generateSlugFromName();
  }

  /**
   * Generate slug from category name
   */
  private String generateSlugFromName() {
    if (name == null) {
      return "category-" + System.currentTimeMillis();
    }

    return name.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

  /**
   * Get unique key for category code validation
   */
  public String getUniqueKey(UUID vendorId) {
    return code + "_" + vendorId;
  }

  /**
   * Check if this is a root category
   */
  public boolean isRootCategory() {
    return parentId == null;
  }
}
