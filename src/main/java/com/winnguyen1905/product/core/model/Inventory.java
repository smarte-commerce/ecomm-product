package com.winnguyen1905.product.core.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record Inventory(
    UUID id,
    String createdDate,
    String updatedDate,
    Boolean isDeleted,
    int quantitySold,
    int quantityReserved,
    int quantityAvailable) implements AbstractModel {
}
