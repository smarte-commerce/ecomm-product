package com.winnguyen1905.product.core.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.product.common.constant.ProductImageType;

public record ProductImageVm(
    UUID id,
    UUID productVariantId,
    String url,
    ProductImageType type) {
}
