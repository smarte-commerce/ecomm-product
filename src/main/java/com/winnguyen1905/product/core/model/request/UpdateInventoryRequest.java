package com.winnguyen1905.product.core.model.request;

public record UpdateInventoryRequest(
    Integer quantityAvailable,
    Integer quantityReserved,
    Integer quantitySold
) {}
