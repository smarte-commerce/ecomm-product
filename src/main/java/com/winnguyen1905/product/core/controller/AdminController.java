package com.winnguyen1905.product.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  
  // @Autowired
  // private InventoryService inventoryService;

  // @GetMapping("/inventories")
  // public ResponseEntity<List<Inventory>> getAllInventories() {
  //   return ResponseEntity.ok(inventoryService.findAll());
  // }

  // @GetMapping("/inventories/{inventoryId}")
  // public ResponseEntity<Inventory> getInventoryById(@PathVariable Long inventoryId) {
  //   return inventoryService.findById(inventoryId)
  //       .map(ResponseEntity::ok)
  //       .orElse(ResponseEntity.notFound().build());
  // }

  // @DeleteMapping("/inventories/{inventoryId}")
  // public ResponseEntity<Void> deleteInventory(@PathVariable Long inventoryId) {
  //   inventoryService.deleteById(inventoryId);
  //   return ResponseEntity.noContent().build();
  // }
}
