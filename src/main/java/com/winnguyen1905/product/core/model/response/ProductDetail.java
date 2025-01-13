package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.core.model.AbstractModel;
import com.winnguyen1905.product.core.model.ProductImage;
import com.winnguyen1905.product.core.model.ProductVariantDetail;

import lombok.Builder;

@Builder
public record ProductDetail(
    UUID id,
    String name,
    String slug,
    String brand,
    List<ProductImage> images,
    Double price,
    String category,
    JsonNode features,
    Boolean isDeleted,
    String productType,
    String description,
    String createdDate,
    List<ProductVariantDetail> variations,
    String updatedDate) implements AbstractModel {
}
