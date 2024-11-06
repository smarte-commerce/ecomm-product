package com.winnguyen1905.product.core.service;

import org.springframework.stereotype.Service;

@Service
public class RedisService {
    public void initializeRedis() {
        // var inventories = inventoryRepository.findAll();
    
        // List<RInventory> rInventories =
        //     Utility.stream(inventories)
        //         .map(
        //             eInventory ->
        //                 RInventory.builder()
        //                     .inventory(
        //                         Inventory.builder()
        //                             .quantityAvailable(eInventory.getQuantityAvailable())
        //                             .sku(eInventory.getSku())
        //                             .id(eInventory.getId())
        //                             .variantId(eInventory.getVariant().getId())
        //                             .quantitySold(eInventory.getQuantitySold())
        //                             .build())
        //                     .variantId(eInventory.getVariant().getId())
        //                     .build())
        //         .toList();
    
        // try {
        //   inventoryRedisRepository.deleteAll();
        //   inventoryRedisRepository.saveAll(rInventories);
        // } catch (Exception e) {
    
        //   log.info("Unable to connect to Redis ::: {}", e.getMessage());
        // }
      }
}