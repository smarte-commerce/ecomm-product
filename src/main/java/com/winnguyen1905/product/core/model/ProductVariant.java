package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.core.model.response.Category;

import io.micrometer.common.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
public record ProductVariant(
  UUID id,
  UUID productId,
  String name,
  String description,
  Brand brand,
  double price,
  Category category,
  String categoryTree,
  Object features,
  List<ProductImage> images,
  Inventory inventory
) implements AbstractModel {
}
