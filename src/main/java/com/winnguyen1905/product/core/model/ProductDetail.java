package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;

@Builder
public record ProductDetail(
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
    List<Variant> variations,
    List<Inventory> inventories,
    String updatedDate) implements AbstractModel {
}
