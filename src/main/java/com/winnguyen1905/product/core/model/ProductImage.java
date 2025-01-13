package com.winnguyen1905.product.core.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record ProductImage(UUID id, String url, UUID productVariantId, String type) implements AbstractModel {}
