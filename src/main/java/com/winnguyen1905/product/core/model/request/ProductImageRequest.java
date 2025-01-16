package com.winnguyen1905.product.core.model.request;

import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record ProductImageRequest(UUID id, String url, UUID productVariantId, String type) implements AbstractModel {}
