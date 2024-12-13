package com.winnguyen1905.product.core.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record Inventory(
    UUID id,
    String sku,
    String createdDate,
    String updatedDate,
    Boolean isDeleted, 
    int stock) implements AbstractModel {
}
