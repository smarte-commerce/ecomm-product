package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.common.ProductType;
import com.winnguyen1905.product.core.model.AbstractModel;
import com.winnguyen1905.product.core.model.Inventory;
import com.winnguyen1905.product.core.model.ProductImage;
import com.winnguyen1905.product.core.model.Variation;

import jakarta.annotation.Nonnull;

public record AddProductRequest(UUID id,
    @Nonnull String name,
    String slug,
    String brand,
    String thumb,
    @Nonnull Double price,
    @Nonnull ProductType productType,
    String description,
    List<Variation> variations,
    List<Inventory> inventories,
    List<ProductImage> images) implements AbstractModel {
}
