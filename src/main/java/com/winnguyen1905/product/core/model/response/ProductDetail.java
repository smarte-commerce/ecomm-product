package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.core.model.AbstractModel;
import com.winnguyen1905.product.core.model.ProductVariantDetail;
import com.winnguyen1905.product.core.model.request.ProductImageRequest;

import lombok.Builder;

@Builder
public record ProductDetail(
    UUID id,
    String name,
    String slug,
    String brand,
    List<ProductImageRequest> images,
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
