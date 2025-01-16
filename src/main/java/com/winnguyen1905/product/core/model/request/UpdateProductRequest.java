package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;
import com.winnguyen1905.product.core.model.response.ProductVariantReview;

public record UpdateProductRequest(
    UUID id,
    String name,
    String slug,
    String brand,
    String thumb,
    Double price,
    String productType,
    String description,
    Boolean isDraft, 
    Boolean isPublished,
    List<ProductVariantReview> variations,
    List<ProductImageRequest> images) implements AbstractModel {
}
    