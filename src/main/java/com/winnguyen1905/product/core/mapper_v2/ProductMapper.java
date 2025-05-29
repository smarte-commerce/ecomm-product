package com.winnguyen1905.product.core.mapper_v2;

import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.ProductImageRequest;
import com.winnguyen1905.product.core.model.request.ProductVariantDto;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EInventory;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

  public static ProductDetailVm toProductDetail(EProduct product) {
    HashMap<String, Integer> stockMapBySku = product.getInventories().stream()
        .collect(Collectors.toMap(EInventory::getSku, EInventory::getQuantityAvailable, (a, b) -> b, HashMap::new));

    List<ProductVariantDetailVm> variations = product.getVariations().stream()
        .map(productVariant -> ProductMapper.productVariantDetailBuilder(productVariant,
            stockMapBySku.get(productVariant.getSku())))
        .collect(Collectors.toList());

    List<ProductImageRequest> images = product.getImages().stream().map(ProductImageMapper::toProductImage)
        .collect(Collectors.toList());

    return ProductDetailVm.builder()
        .id(product.getId())
        .name(product.getName())
        .images(images)
        .variations(variations)
        .description(product.getDescription())
        .build();
  }

  private static ProductVariantDetailVm productVariantDetailBuilder(EProductVariant productVariant, int stock) {
    return ProductVariantDetailVm.builder()
        .stock(stock)
        .id(productVariant.getId())
        .sku(productVariant.getSku())
        .price(productVariant.getPrice())
        .productId(productVariant.getProduct().getId())
        .features(productVariant.getFeatures())
        .build();
  }

  public static EProductVariant toProductVariantEntity(ProductVariantDto productVariantDetail) {
    return EProductVariant.builder()
        .price(productVariantDetail.price())
        .sku(productVariantDetail.sku())
        .features(productVariantDetail.features())
        .build();
  }

  public static ProductVariantReviewVm toProductVariantReview(ESProductVariant esProductVariant) {
    return ProductVariantReviewVm.builder()
        .stock(esProductVariant.getInventory().getQuantityAvailable())
        .id(esProductVariant.getId())
        .sku(esProductVariant.getInventory().getSku())
        .price(esProductVariant.getPrice())
        .productId(esProductVariant.getProductId())
        .features(esProductVariant.getFeatures())
        .build();
  }
}
