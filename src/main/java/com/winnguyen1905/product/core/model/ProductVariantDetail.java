package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.core.model.request.ProductImageRequest;

import lombok.Builder;

@Builder
public record ProductVariantDetail(
    UUID id,
    int stock,
    String sku,
    double price,
    UUID productId,
    JsonNode features,
    List<ProductImageRequest> images
    ) implements AbstractModel {
}
