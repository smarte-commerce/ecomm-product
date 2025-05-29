package com.winnguyen1905.product.core.mapper_v2;

import com.winnguyen1905.product.common.constant.ProductImageType;
import com.winnguyen1905.product.core.model.request.ProductImageRequest;
import com.winnguyen1905.product.persistance.entity.EProductImage;

public class ProductImageMapper {
  public static ProductImageRequest toProductImage(EProductImage image) {
    if (image == null) return null;
    return ProductImageRequest.builder()
        .url(image.getUrl())
        .type(image.getType())
        .productVariantId(image.getProductVariantId())
        .build();
  }

  public static EProductImage toProductImageEntity(ProductImageRequest productImageRequest) {
    if (productImageRequest == null) return null;
    return EProductImage.builder()
        .url(productImageRequest.url())
        .type(productImageRequest.type())
        .productVariantId(productImageRequest.productVariantId())
        .build();
  }
}
