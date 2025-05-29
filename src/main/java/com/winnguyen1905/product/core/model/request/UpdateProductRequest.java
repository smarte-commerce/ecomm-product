package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

public record UpdateProductRequest(
    UUID id,
    String name,
    String description,
    String slug,
    String categoryCode,
    String brandCode,
    JsonNode features,
    Boolean isPublished
// List<ProductVariantRequest> variations,
// List<ProductInventoryRequest> inventories,
// List<ProductImageRequest> images
) implements AbstractModel {
}
