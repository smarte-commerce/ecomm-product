package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;

@Builder
public record ProductVariantDetail(
    UUID id,
    int stock,
    String sku,
    double price,
    UUID productId,
    JsonNode features,
    List<ProductImage> images
    ) implements AbstractModel {
}
