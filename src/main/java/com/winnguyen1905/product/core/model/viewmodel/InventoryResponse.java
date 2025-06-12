package com.winnguyen1905.product.core.model.viewmodel;

import java.util.UUID;

public record InventoryResponse(
    UUID id,
    String sku,
    Integer quantityAvailable,
    Integer quantityReserved,
    Integer quantitySold,
    UUID productId,
    Long version
) {}
