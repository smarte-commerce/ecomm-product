package com.winnguyen1905.product.core.service;

import com.winnguyen1905.product.persistance.entity.EInventory;
import reactor.core.publisher.Mono;

public interface InventoryValidationService {
    Mono<EInventory> validateAndGetInventory(String sku, int quantity);
    Mono<Boolean> reserveInventory(String sku, int quantity);
}
