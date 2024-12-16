package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.common.ProductType;
import com.winnguyen1905.product.core.model.AbstractModel;
import com.winnguyen1905.product.core.model.Brand;
import com.winnguyen1905.product.core.model.Inventory;
import com.winnguyen1905.product.core.model.ProductImage;
import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.response.Category;

import jakarta.annotation.Nonnull;

public record AddProductRequest(
    UUID id,
    String slug,
    String thumb,
    Brand brand,
    Object features,
    Category category,
    String description,
    @Nonnull String name,
    @Nonnull Double price,
    @Nonnull ProductType productType,
    @Nonnull List<ProductVariant> variations,
    List<Inventory> inventories,
    List<ProductImage> images) implements AbstractModel {
}
