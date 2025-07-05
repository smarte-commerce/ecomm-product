package com.winnguyen1905.product.core.mapper_v2;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.persistance.elasticsearch.ESInventory;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.util.CommonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductESMapper {

  public static List<ESProductVariant> toESProductVariants(EProduct product) {
    if (product == null) return null;

    HashMap<String, EInventory> inventoryMapBySku = product.getInventories().stream()
        .collect(
            Collectors.toMap(EInventory::getSku, inventory -> inventory, (sku, inventory) -> inventory, HashMap::new));

    // Map product images by variant ID
    HashMap<UUID, String> imageMapByProductVariantId = product.getImages().stream()
        .collect(
            Collectors.toMap(EProductImage::getProductVariantId, EProductImage::getUrl,
            (a, b) -> b, HashMap::new));

    return CommonUtils.stream(product.getVariants()) // Using 'variants' instead of 'variations'
        .map(productVariant -> {
              ObjectNode allFeatures = mergeProductFeatures(product, productVariant);
              StringBuilder variantName = generateVariantName(product, productVariant);
              ESInventory inventory = InventoryMapper.toESInventory(inventoryMapBySku.get(productVariant.getSku()));
              JsonNode mergedFeatures = transformFeaturesToObject(allFeatures);

              return ESProductVariant.builder()
                  .region(productVariant.getRegion()) // Use variant's region (RegionPartition enum)
                  .id(productVariant.getId())
                  .productId(product.getId())
                  .features(mergedFeatures)
                  .imageUrl(imageMapByProductVariantId.get(productVariant.getId()))
                  .brand(product.getBrand() != null ? product.getBrand().getName() : null)
                  .price(productVariant.getPrice() != null ? productVariant.getPrice().doubleValue() : 0.0)
                  .name(variantName.toString())
                  .description(product.getDescription())
                  .inventory(inventory)
                  .createdBy(productVariant.getCreatedBy())
                  .updatedBy(productVariant.getUpdatedBy())
                  .createdDate(productVariant.getCreatedDate())
                  .updatedDate(productVariant.getUpdatedDate())
                  .build();
            })
        .collect(Collectors.toList());
  }

  private static JsonNode transformFeaturesToObject(ObjectNode allFeatures) {
    JsonNode mergedFeatures;
    var mapper = new ObjectMapper();
    try {
      mergedFeatures = mapper.treeToValue(allFeatures, JsonNode.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return mergedFeatures;
  }

  private static StringBuilder generateVariantName(EProduct product, EProductVariant productVariant) {
    var variantName = new StringBuilder(product.getName());

    // Handle variant attributes (using attributes instead of features)
    if (productVariant.getAttributes() != null) {
      var fieldIterator = ((JsonNode) productVariant.getAttributes()).fieldNames();
      while (fieldIterator.hasNext()) {
        var fieldName = fieldIterator.next();
        var value = ((JsonNode) productVariant.getAttributes()).get(fieldName).textValue();
        variantName.append(" ").append(value).append(" ").append(fieldName);
      }
    }
    return variantName;
  }

  private static ObjectNode mergeProductFeatures(EProduct product, EProductVariant productVariant) {
    var baseFields = product.getFeatures();
    var variantFields = productVariant.getAttributes(); // Using attributes for variants

    ObjectNode allFeatures = new ObjectMapper().createObjectNode();

    // Merge product features
    if (baseFields instanceof JsonNode) {
      allFeatures.setAll((ObjectNode) baseFields);
    }

    // Merge variant attributes
    if (variantFields instanceof JsonNode) {
      allFeatures.setAll((ObjectNode) variantFields);
    }

    return allFeatures;
  }

  public static ProductVariantReviewVm toProductVariantReview(ESProductVariant esProductVariant) {
    return ProductVariantReviewVm.builder()
        .id(esProductVariant.getId())
        .imageUrl(esProductVariant.getImageUrl())
        .productId(esProductVariant.getProductId())
        .features(esProductVariant.getFeatures())
        .price(esProductVariant.getPrice())
        .name(esProductVariant.getName())
        .stock(esProductVariant.getInventory().getQuantityAvailable())
        .build();
  }

  public static List<ProductVariantReviewVm> toProductVariantReviews(List<ESProductVariant> esProductVariants) {
    return CommonUtils.stream(esProductVariants)
        .map(ProductESMapper::toProductVariantReview)
        .collect(Collectors.toList());
  }
}
