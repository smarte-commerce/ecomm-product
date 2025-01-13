package com.winnguyen1905.product.core.mapper_v2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.winnguyen1905.product.core.model.ProductImage;
import com.winnguyen1905.product.persistance.entity.EProductImage;

public class ProductImageMapper {
  public static ProductImage toProductImage(EProductImage image) {
    if (image == null) return null;
    return ProductImage.builder()
        .id(image.getId())
        .url(image.getUrl())
        .type(image.getType().name())
        .productVariantId(image.getProductVariantId())
        .build();
  }
}
