package com.winnguyen1905.product.core.model.response;

import com.winnguyen1905.product.common.constant.ProductImageType;
import java.util.UUID;

public record ProductImageResponse(
    UUID id,
    String url,
    ProductImageType type,
    UUID productId,
    UUID productVariantId,
    Long version
) {}
