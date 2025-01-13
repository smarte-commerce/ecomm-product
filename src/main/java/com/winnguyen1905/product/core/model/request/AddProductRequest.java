package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.common.ProductType;
import com.winnguyen1905.product.core.model.AbstractModel;
import com.winnguyen1905.product.core.model.Brand;
import com.winnguyen1905.product.core.model.Inventory;
import com.winnguyen1905.product.core.model.ProductImage;
import com.winnguyen1905.product.core.model.ProductVariantDetail;


public record AddProductRequest(
    UUID id,
    String slug,
    String thumb,
    String categoryCode,
    JsonNode features,
    String brandCode,
    String description,
    String name,
    String productType,
    List<ProductVariantDetail> variations,
    List<Inventory> inventories,
    List<ProductImage> images) {
}
