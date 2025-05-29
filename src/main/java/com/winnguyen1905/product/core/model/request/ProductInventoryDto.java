package com.winnguyen1905.product.core.model.request;

import java.util.UUID;

public record ProductInventoryDto(
    UUID id,
    String sku,
    String createdDate,
    String updatedDate,
    Boolean isDeleted,
    int quantitySold,
    int quantityReserved,
    int quantityAvailable) {
}
