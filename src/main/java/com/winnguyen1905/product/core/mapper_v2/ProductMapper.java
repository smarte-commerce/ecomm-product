package com.winnguyen1905.product.core.mapper_v2;

import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.ProductVariantDto;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantByShopVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EInventory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductMapper {

  public static ProductDetailVm toProductDetail(EProduct product) {
    HashMap<String, Integer> stockMapBySku = product.getInventories().stream()
        .collect(Collectors.toMap(EInventory::getSku, EInventory::getQuantityAvailable, (a, b) -> b, HashMap::new));

    List<ProductVariantDetailVm> variations = product.getVariations().stream()
        .map(productVariant -> ProductMapper.productVariantDetailBuilder(productVariant,
            stockMapBySku.get(productVariant.getSku())))
        .collect(Collectors.toList()); 
        
    return ProductDetailVm.builder()
        .id(product.getId())
        .name(product.getName())
        .productType(product.getProductType())
        // .images(images)
        .variations(variations)
        .description(product.getDescription())
        .build();
  }

  public static ProductVariantReviewVm toProductVariantReview(EProduct product) {
    if (product.getVariations().isEmpty()) {
      throw new RuntimeException("No variants found for product: " + product.getId());
    }

    // Create a map of SKU to quantity available for O(1) lookups
    Map<String, Integer> stockMapBySku = product.getInventories().stream()
        .collect(Collectors.toMap(
            EInventory::getSku,
            EInventory::getQuantityAvailable,
            (existing, replacement) -> replacement,
            HashMap::new));

    // Get the first variant (or implement your variant selection logic here)
    EProductVariant variant = product.getVariations().get(0);

    return ProductVariantReviewVm.builder()
        .id(variant.getId())
        .sku(variant.getSku())
        .price(variant.getPrice())
        .productId(product.getId())
        .features(variant.getFeatures())
        .stock(stockMapBySku.getOrDefault(variant.getSku(), 0))
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

  public static ProductVariantReviewVm toProductVariantReview(EProductVariant productVariant) {
    return ProductVariantReviewVm.builder()
        .id(productVariant.getId())
        .sku(productVariant.getSku())
        .price(productVariant.getPrice())
        .productId(productVariant.getProduct().getId())
        .features(productVariant.getFeatures())
        .stock(0) // Default stock to 0, should be set from inventory if available
        .build();
  }

  public static ProductVariantByShopVm toProductVariantByShopVm(List<EProductVariant> productVariants) {
    List<ProductVariantByShopVm.ShopProductVariant> shopProductVariants = productVariants.stream()
        .collect(Collectors.groupingBy(productVariant -> productVariant.getProduct().getShopId()))
        .entrySet().stream()
        .map(entry -> ProductVariantByShopVm.ShopProductVariant.builder()
            .shopId(entry.getKey())
            .productVariantReviews(entry.getValue().stream()
                .map(ProductMapper::toProductVariantReview)
                .collect(Collectors.toList()))
            .build())
        .collect(Collectors.toList());

    return new ProductVariantByShopVm(shopProductVariants);
  }
}
