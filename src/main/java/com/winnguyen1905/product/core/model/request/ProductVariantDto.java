package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record ProductVariantDto(
    int stock,
    String sku,
    double price,
    JsonNode features,
    List<ProductImageRequest> images) implements AbstractModel {

}
