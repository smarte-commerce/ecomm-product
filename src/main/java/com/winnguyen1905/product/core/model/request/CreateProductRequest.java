package com.winnguyen1905.product.core.model.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.secure.RegionPartition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateProductRequest(

    @NotBlank(message = "Tên sản phẩm là bắt buộc") @Size(min = 2, max = 255, message = "Tên sản phẩm phải từ 2-255 ký tự") String name,

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự") String description,

    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự") String shortDescription,

    @Size(max = 300, message = "Slug không được vượt quá 300 ký tự") String slug,

    @NotNull(message = "Loại sản phẩm là bắt buộc") ProductType productType,

    @NotNull(message = "Vendor ID là bắt buộc") UUID vendorId,

    @NotNull(message = "Shop ID là bắt buộc") UUID shopId,

    @NotNull(message = "Region là bắt buộc") RegionPartition region,

    UUID brandId,

    UUID categoryId,

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá cơ bản phải lớn hơn 0") BigDecimal basePrice,

    Object features,

    Object specifications,

    // Product dimensions
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

    // Product variants
    @Valid List<CreateProductVariantRequest> variants,

    // Product images
    @Valid List<CreateProductImageRequest> images) {

  public CreateProductRequest {
    if (trackInventory == null)
      trackInventory = true;
    if (allowBackorder == null)
      allowBackorder = false;
    if (lowStockThreshold == null)
      lowStockThreshold = 10;
    if (requiresShipping == null)
      requiresShipping = true;
  }
}
