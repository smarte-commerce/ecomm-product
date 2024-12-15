package com.winnguyen1905.product.core.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.winnguyen1905.product.core.model.Brand;
import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.response.Category;
import com.winnguyen1905.product.persistance.elasticsearch.ESCategory;
import com.winnguyen1905.product.persistance.elasticsearch.ESInventory;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EVariation;
import com.winnguyen1905.product.util.CommonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductESMapper {

  private final CategoryMapper categoryMapper;
  private final InventoryMapper inventoryMapper;

  public List<ESProductVariant> toESProductVariants(EProduct product) {

    return CommonUtils.stream(product.getVariations())
        .map(
            item -> {
              ObjectNode allFeatures = mergeProductFeatures(product, item);
              StringBuilder variantName = generateVariantName(product, item);
              ESInventory inventory = getVariantInventory(product, item);
              Object mergedFeatures = transformFeaturesToObject(allFeatures);

              return ESProductVariant.builder()
                  .id(item.getId())
                  .productId(product.getId())
                  .features(mergedFeatures)
                  .brand(product.getBrand().getName())
                  .price(item.getPrice())
                  .name(variantName.toString())
                  .description(product.getDescription())
                  .category(this.categoryMapper.toESCategory(product.getCategory()))
                  .inventory(inventory)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private Object transformFeaturesToObject(ObjectNode allFeatures) {
    Object mergedFeatures;
    var mapper = new ObjectMapper();
    try {
      mergedFeatures = mapper.treeToValue(allFeatures, Object.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return mergedFeatures;
  }

  private ESInventory getVariantInventory(EProduct product, EVariation item) {
    return product.getInventories().stream()
        .filter(inventory -> inventory.getSku().equals(item.getSku()))
        .map(this.inventoryMapper::toESInventory)
        .findFirst()
        .orElseThrow();
  }

  private StringBuilder generateVariantName(EProduct product, EVariation item) {
    var fieldIterator = item.getFeatureValues().fieldNames();

    var variantName = new StringBuilder(product.getName());
    while (fieldIterator.hasNext()) {
      var fieldName = fieldIterator.next();
      var value = item.getFeatureValues().get(fieldName).textValue();
      variantName.append(" ").append(value).append(" ").append(fieldName);
    }
    return variantName;
  }

  private ObjectNode mergeProductFeatures(EProduct product, EVariation item) {
    var baseFields = product.getFeatures();
    var variantFields = item.getFeatureValues();

    ObjectNode allFeatures = baseFields.deepCopy();
    allFeatures.setAll((ObjectNode) variantFields);
    return allFeatures;
  }

  private ProductVariant with(ESProductVariant esProductVariant) {
    return ProductVariant.builder()
        .id(esProductVariant.getId())
        .productId(esProductVariant.getProductId())
        .features(esProductVariant.getFeatures())
        .brand(Brand.builder().name(esProductVariant.getBrand()).build())
        .price(esProductVariant.getPrice())
        .name(esProductVariant.getName())
        .description(esProductVariant.getDescription())
        .category(
            Category.builder()
                .name(esProductVariant.getCategory().getName())
                .id(esProductVariant.getCategory().getId())
                .build())
        .inventory(this.inventoryMapper.toInventory(esProductVariant.getInventory()))
        .build();
  }

  private List<ProductVariant> with(List<ESProductVariant> esProductVariants) {
    return CommonUtils.stream(esProductVariants)
        .map(this::with)
        .toList();
  }
}
