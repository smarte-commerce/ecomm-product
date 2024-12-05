package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;
import com.winnguyen1905.product.core.model.ProductImage;
import com.winnguyen1905.product.core.model.Variation;

public record UpdateProductRequest(
    UUID id,
    String name,
    String slug,
    String brand,
    String thumb,
    Double price,
    String productType,
    String description,
    List<Variation> variations,
    List<ProductImage> images, 
    Boolean isDraft, 
    Boolean isPublished) implements AbstractModel {
}
