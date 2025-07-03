package com.winnguyen1905.product.core.mapper_v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.ProductVariantDto;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantByShopVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EInventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductMapper {

  public static ProductDetailVm toProductDetail(EProduct product) {
    if (product == null) return null;

    // Create stock map from inventories
    HashMap<String, Integer> stockMapBySku = product.getInventories().stream()
        .collect(Collectors.toMap(EInventory::getSku, EInventory::getQuantityAvailable, (a, b) -> b, HashMap::new));

    // Map variants (using 'variants' instead of 'variations')
    List<ProductVariantDetailVm> variations = product.getVariants().stream()
        .map(productVariant -> ProductMapper.productVariantDetailBuilder(productVariant,
            stockMapBySku.get(productVariant.getSku())))
        .collect(Collectors.toList());

    return ProductDetailVm.builder()
        .id(product.getId())
        .name(product.getName())
        .slug(product.getSlug())
        .brand(product.getBrand() != null ? product.getBrand().getName() : null)
        .price(product.getMinPrice() != null ? product.getMinPrice().doubleValue() : null)
        .category(product.getCategory() != null ? product.getCategory().getName() : null)
        .features((JsonNode) product.getFeatures())
        .isDeleted(product.getIsDeleted())    
        .productType(product.getProductType())
        .description(product.getDescription())
        .createdDate(product.getCreatedDate() != null ? product.getCreatedDate().toString() : null)
        .updatedDate(product.getUpdatedDate() != null ? product.getUpdatedDate().toString() : null)
        .variations(variations)
        .build();
  }

  public static ProductVariantReviewVm toProductVariantReview(EProduct product) {
    if (product == null || product.getVariants().isEmpty()) {
      throw new RuntimeException("No variants found for product: " + (product != null ? product.getId() : "null"));
    }

    // Create a map of SKU to quantity available for O(1) lookups
    Map<String, Integer> stockMapBySku = product.getInventories().stream()
        .collect(Collectors.toMap(
            EInventory::getSku,
            EInventory::getQuantityAvailable,
            (existing, replacement) -> replacement,
            HashMap::new));

    // Get the first variant (or implement your variant selection logic here)
    EProductVariant variant = product.getVariants().get(0);

    return ProductVariantReviewVm.builder()
        .id(variant.getId())
        .sku(variant.getSku())
        .price(variant.getPrice() != null ? variant.getPrice().doubleValue() : 0.0)
        .productId(product.getId())
        .features(variant.getFeatures())
        .stock(stockMapBySku.getOrDefault(variant.getSku(), 0))
        .build();
  }

  private static ProductVariantDetailVm productVariantDetailBuilder(EProductVariant productVariant, Integer stock) {
    if (productVariant == null) return null;

    return ProductVariantDetailVm.builder()
        .stock(stock != null ? stock : 0)
        .id(productVariant.getId())
        .sku(productVariant.getSku())
        .price(productVariant.getPrice() != null ? productVariant.getPrice().doubleValue() : 0.0)
        .productId(productVariant.getProduct() != null ? productVariant.getProduct().getId() : null)
        .features((JsonNode)    productVariant.getFeatures())
        .build();
  }

  public static EProductVariant toProductVariantEntity(ProductVariantDto productVariantDetail) {
    if (productVariantDetail == null) return null;

    return EProductVariant.builder()
        .price(productVariantDetail.price())
        .sku(productVariantDetail.sku())
        .attributes(productVariantDetail.features()) // Using attributes instead of features
        .build();
  }

  /**
   * Convert AddProductRequest to EProduct entity
   */
  public static EProduct toProductEntity(AddProductRequest request) {
    if (request == null) return null;

    return EProduct.builder()
        .name(request.name())
        .description(request.description())
        .slug(request.slug())
        .features(request.features())
        .specifications((JsonNode) request.features())
        .status(ProductStatus.DRAFT)
        .isPublished(false)
        .isDeleted(false)
        .trackInventory(true)
        .allowBackorder(false)
        .requiresShipping(true)
        .lowStockThreshold(10)
        .viewCount(0L)
        .purchaseCount(0L)
        .ratingCount(0)
        .ratingAverage(BigDecimal.ZERO)
        .build();
  }

  /**
   * Update EProduct entity from UpdateProductRequest
   */
  public static void updateProductFromRequest(EProduct product, UpdateProductRequest request) {
    if (product == null || request == null) return;

    if (request.name() != null) {
      product.setName(request.name());
    }
    if (request.description() != null) {
      product.setDescription(request.description());
    }
    if (request.slug() != null) {
      product.setSlug(request.slug());
    }
    if (request.features() != null) {
      product.setFeatures(request.features());
    }
    if (request.isPublished() != null) {
      product.setIsPublished(request.isPublished());
    }
  }

  /**
   * Set brand for product entity
   */
  public static void setProductBrand(EProduct product, EBrand brand) {
    if (product != null) {
      product.setBrand(brand);
    }
  }

  /**
   * Set category for product entity
   */
  public static void setProductCategory(EProduct product, ECategory category) {
    if (product != null) {
      product.setCategory(category);
    }
  }

  /**
   * Convert list of EProduct to list of ProductDetailVm
   */
  public static List<ProductDetailVm> toProductDetailList(List<EProduct> products) {
    if (products == null) return new ArrayList<>();

    return products.stream()
        .map(ProductMapper::toProductDetail)
        .collect(Collectors.toList());
  }

  public static ProductVariantReviewVm toProductVariantReview(ESProductVariant esProductVariant) {
    return ProductVariantReviewVm.builder()
        .stock(esProductVariant.getInventory().getQuantityAvailable())
        .id(esProductVariant.getId())
        .sku(esProductVariant.getInventory().getSku())
        .price(esProductVariant.getPrice())
        .productId(esProductVariant.getProductId())
        .features((JsonNode) esProductVariant.getFeatures())
        .build();
  }

  public static ProductVariantReviewVm toProductVariantReview(EProductVariant productVariant) {
    return ProductVariantReviewVm.builder()
        .id(productVariant.getId())
        .sku(productVariant.getSku())
        .price(productVariant.getPrice())
        .productId(productVariant.getProduct().getId())
        .features((JsonNode)  productVariant.getFeatures())
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
