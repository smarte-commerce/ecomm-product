package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.secure.RegionPartition;

public record AddProductRequest(
    UUID id,
    String name,
    RegionPartition region,
    String description,
    String slug,
    UUID shopId,
    ProductType type,
    String brandCode,
    JsonNode features,
    Boolean isPublished,
    List<ProductVariantDto> variations,
    List<ProductInventoryDto> inventories,
    List<ProductImageRequest> images
) implements AbstractModel {
}
