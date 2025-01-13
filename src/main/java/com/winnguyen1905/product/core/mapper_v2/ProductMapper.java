package com.winnguyen1905.product.core.mapper_v2;

import com.winnguyen1905.product.core.model.ProductVariantDetail;
import com.winnguyen1905.product.core.model.response.ProductDetail;
import com.winnguyen1905.product.core.model.response.ProductVariantReview;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.core.model.ProductImage;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

  public static ProductDetail toProductDetail(EProduct product) {
    HashMap<String, Integer> stockMapBySku = product.getInventories().stream()
        .collect(Collectors.toMap(EInventory::getSku, EInventory::getQuantityAvailable, (a, b) -> b, HashMap::new));

    List<ProductVariantDetail> variations = product.getVariations().stream()
        .map(productVariant -> ProductMapper.productVariantDetailBuilder(productVariant,
            stockMapBySku.get(productVariant.getSku())))
        .collect(Collectors.toList());

    List<ProductImage> images = product.getImages().stream().map(ProductImageMapper::toProductImage)
        .collect(Collectors.toList());

    return ProductDetail.builder()
        .id(product.getId())
        .name(product.getName())
        .images(images)
        .variations(variations)
        .description(product.getDescription())
        .build();
  }

  private static ProductVariantDetail productVariantDetailBuilder(EProductVariant productVariant, int stock) {
    return ProductVariantDetail.builder()
        .stock(stock)
        .id(productVariant.getId())
        .sku(productVariant.getSku())
        .price(productVariant.getPrice())
        .productId(productVariant.getProduct().getId())
        .features(productVariant.getFeatures())
        .build();
  }

  public static ProductVariantReview toProductVariantReview(ESProductVariant esProductVariant) {
    return ProductVariantReview.builder()
        .stock(esProductVariant.getInventory().getQuantityAvailable())
        .id(esProductVariant.getId())
        .sku(esProductVariant.getInventory().getSku())
        .price(esProductVariant.getPrice())
        .productId(esProductVariant.getProductId())
        .features(esProductVariant.getFeatures())
        .build();
  }
}
