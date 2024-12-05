package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

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
    List<Variation> variations,
    String createdDate,
    String updatedDate) implements AbstractModel {
}
