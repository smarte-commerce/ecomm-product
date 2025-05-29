package com.winnguyen1905.product.core.service.impl;

public record InsufficientInventoryException(
    String message,
    String productId,
    String sku,
    int quantityAvailable,
    int quantityReserved,
    int quantitySold) {

  
}
