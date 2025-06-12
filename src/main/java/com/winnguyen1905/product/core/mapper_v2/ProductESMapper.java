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
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.entity.garbage.EProductImage;
import com.winnguyen1905.product.secure.TAccountRequest;
import com.winnguyen1905.product.util.CommonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductESMapper {

  public static List<ESProductVariant> toESProductVariants(EProduct product) {
    HashMap<String, EInventory> inventoryMapBySku = product.getInventories().stream()
        .collect(
            Collectors.toMap(EInventory::getSku, inventory -> inventory, (sku, inventory) -> inventory, HashMap::new));
    // HashMap<UUID, String> imageMapByProductVariantId =
    // product.getImages().stream()
    // .collect(
    // Collectors.toMap(EProductImage::getProductVariantId, EProductImage::getUrl,
    // (a, b) -> b, HashMap::new));

    return CommonUtils.stream(product.getVariations())
        .map(
            productVariant -> {
              ObjectNode allFeatures = mergeProductFeatures(product, productVariant);
              StringBuilder variantName = generateVariantName(product, productVariant);
              ESInventory inventory = InventoryMapper.toESInventory(inventoryMapBySku.get(productVariant.getSku()));
              JsonNode mergedFeatures = transformFeaturesToObject(allFeatures);

              return ESProductVariant.builder()
                  .region(product.getRegion())
                  .id(productVariant.getId())
                  .productId(product.getId())
                  .features(mergedFeatures)
                  // .imageUrl(imageMapByProductVariantId.get(productVariant.getId()))
                  // .brand(product.getBrand().getName())
                  .price(productVariant.getPrice())
                  .name(variantName.toString())
                  .description(product.getDescription())
                  .inventory(inventory)
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
    var fieldIterator = ((JsonNode) productVariant.getFeatures()).fieldNames();
    var variantName = new StringBuilder(product.getName());
    while (fieldIterator.hasNext()) {
      var fieldName = fieldIterator.next();
      var value = ((JsonNode) productVariant.getFeatures()).get(fieldName).textValue();
      variantName.append(" ").append(value).append(" ").append(fieldName);
    }
    return variantName;
  }

  private static ObjectNode mergeProductFeatures(EProduct product, EProductVariant productVariant) {
    var baseFields = product.getFeatures();
    var variantFields = productVariant.getFeatures();

    ObjectNode allFeatures = ((JsonNode) baseFields).deepCopy();
    allFeatures.setAll((ObjectNode) variantFields);
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
