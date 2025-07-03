package com.winnguyen1905.product.core.mapper_v2;

import java.util.List;
import java.util.stream.Collectors;

import com.winnguyen1905.product.core.model.request.ProductImageRequest;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageVm;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;

public class ProductImageMapper {

  /**
   * Convert EProductImage to ProductImageRequest
   */
  public static ProductImageRequest toProductImage(EProductImage image) {
    if (image == null) return null;

    return ProductImageRequest.builder()
        .url(image.getUrl())
        .type(image.getType())
        .productVariantId(image.getProductVariantId())
        .build();
  }

  /**
   * Convert EProductImage to ProductImageVm for view models
   */
  public static ProductImageVm toProductImageVm(EProductImage image) {
    if (image == null) return null;

    return new ProductImageVm(
        image.getId(),
        image.getProductVariantId(),
        image.getUrl(),
        image.getType()
    );
  }

  /**
   * Convert ProductImageRequest to EProductImage entity
   */
  public static EProductImage toProductImageEntity(ProductImageRequest productImageRequest) {
    if (productImageRequest == null) return null;

    return EProductImage.builder()
        .url(productImageRequest.url())
        .type(productImageRequest.type())
        .isDeleted(false)
        .isPrimary(false)
        .displayOrder(0)
        .build();
  }

  /**
   * Create EProductImage entity with variant relationship
   */
  public static EProductImage toProductImageEntity(ProductImageRequest productImageRequest, EProductVariant variant) {
    if (productImageRequest == null) return null;

    EProductImage image = EProductImage.builder()
        .url(productImageRequest.url())
        .type(productImageRequest.type())
        .isDeleted(false)
        .isPrimary(false)
        .displayOrder(0)
        .build();

    // Set relationships
    if (variant != null) {
      image.setVariant(variant);
      image.setProduct(variant.getProduct());
      image.setVendorId(variant.getVendorId());
    }

    return image;
  }

  /**
   * Create EProductImage entity with product relationship
   */
  public static EProductImage toProductImageEntity(ProductImageRequest productImageRequest, EProduct product) {
    if (productImageRequest == null) return null;

    EProductImage image = EProductImage.builder()
        .url(productImageRequest.url())
        .type(productImageRequest.type())
        .isDeleted(false)
        .isPrimary(false)
        .displayOrder(0)
        .build();

    // Set relationships
    if (product != null) {
      image.setProduct(product);
      image.setVendorId(product.getVendorId());
    }

    return image;
  }

  /**
   * Convert list of EProductImage to list of ProductImageVm
   */
  public static List<ProductImageVm> toProductImageVmList(List<EProductImage> images) {
    if (images == null) return null;

    return images.stream()
        .map(ProductImageMapper::toProductImageVm)
        .collect(Collectors.toList());
  }

  /**
   * Convert list of EProductImage to list of ProductImageRequest
   */
  public static List<ProductImageRequest> toProductImageRequestList(List<EProductImage> images) {
    if (images == null) return null;

    return images.stream()
        .map(ProductImageMapper::toProductImage)
        .collect(Collectors.toList());
  }

  /**
   * Update image entity from request
   */
  public static void updateImageFromRequest(EProductImage image, ProductImageRequest request) {
    if (image == null || request == null) return;

    if (request.url() != null) {
      image.setUrl(request.url());
    }
    if (request.type() != null) {
      image.setType(request.type());
    }
  }

  /**
   * Set image as primary for product
   */
  public static void setAsPrimary(EProductImage image) {
    if (image != null) {
      image.setIsPrimary(true);
      image.setDisplayOrder(0);
    }
  }

  /**
   * Create primary product image
   */
  public static EProductImage createPrimaryImage(ProductImageRequest request, EProduct product) {
    EProductImage image = toProductImageEntity(request, product);
    if (image != null) {
      setAsPrimary(image);
    }
    return image;
  }
}
