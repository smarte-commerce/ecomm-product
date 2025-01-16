package com.winnguyen1905.product.core.mapper_v2;

import com.winnguyen1905.product.common.ProductImageType;
import com.winnguyen1905.product.core.model.request.ProductImageRequest;
import com.winnguyen1905.product.persistance.entity.EProductImage;

public class ProductImageMapper {
  public static ProductImageRequest toProductImage(EProductImage image) {
    if (image == null) return null;
    return ProductImageRequest.builder()
        .id(image.getId())
        .url(image.getUrl())
        .type(image.getType().name())
        .productVariantId(image.getProductVariantId())
        .build();
  }

  public static EProductImage toProductImageEntity(ProductImageRequest productImageRequest) {
    if (productImageRequest == null) return null;
    return EProductImage.builder()
        .id(productImageRequest.id())
        .url(productImageRequest.url())
        .type(ProductImageType.valueOf(productImageRequest.type()))
        .productVariantId(productImageRequest.productVariantId())
        .build();
  }
}
