package com.winnguyen1905.product.core.model.response;

import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record ProductVariantReview(
    UUID id,
    int stock,
    String imageUrl,
    String name,
    String sku,
    double price,
    UUID productId,
    Object features) implements AbstractModel {
}
