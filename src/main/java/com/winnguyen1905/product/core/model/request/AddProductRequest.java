package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

public record AddProductRequest(
    UUID id,
    String name,
    String description,
    String slug,
    UUID shopId,
    String categoryCode,
    String brandCode,
    JsonNode features,
    Boolean isPublished,
    List<ProductVariantDto> variations,
    List<ProductInventoryDto> inventories,
    List<ProductImageRequest> images
) implements AbstractModel {
}
