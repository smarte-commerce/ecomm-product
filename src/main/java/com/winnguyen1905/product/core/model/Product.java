package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;

@Builder
public record Product(
    UUID id,
    String name,
    String slug,
    String brand,
    String thumb,
    Double price,
    String category,
    JsonNode features,
    Boolean isDeleted,
    String productType,
    String description,
    String createdDate,
    List<Variation> variations,
    List<Inventory> inventories,
    String updatedDate) implements AbstractModel {
}
