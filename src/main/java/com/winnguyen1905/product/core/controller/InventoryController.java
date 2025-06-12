package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.model.request.UpdateInventoryRequest;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;

  @GetMapping("/product/{productId}")
  @ResponseMessage(message = "Get product inventories success")
  public ResponseEntity<PagedResponse<InventoryVm>> getProductInventories(
      @PathVariable UUID productId, Pageable pageable) {
    return ResponseEntity.ok(inventoryService.getProductInventory(productId, pageable));
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get inventory by id success")
  public ResponseEntity<InventoryVm> getInventoryById(@PathVariable UUID id) {
    return ResponseEntity.ok(inventoryService.getInventoryById(id));
  }

  @GetMapping("/sku/{sku}")
  @ResponseMessage(message = "Get inventory by SKU success")
  public ResponseEntity<InventoryVm> getInventoryBySku(@PathVariable String sku) {
    return ResponseEntity.ok(inventoryService.getInventoryBySku(sku));
  }

  @GetMapping("/products/{productId}")
  @ResponseMessage(message = "Get product inventory success")
  public ResponseEntity<PagedResponse<InventoryVm>> getProductInventory(
      @PathVariable UUID productId,
      Pageable pageable) {
    return ResponseEntity.ok(inventoryService.getProductInventory(productId, pageable));
  }

  @PutMapping("/{id}")
  @ResponseMessage(message = "Update inventory success")
  public ResponseEntity<InventoryVm> updateInventory(
      @PathVariable UUID id,
      @RequestBody UpdateInventoryRequest request) {
    return ResponseEntity.ok(inventoryService.updateInventory(id));
  }

  @PatchMapping("/{id}/reserve")
  @ResponseMessage(message = "Reserve inventory success")
  public Mono<ResponseEntity<InventoryVm>> reserveInventory(
      @PathVariable UUID id,
      @RequestParam Integer quantity) {
    return inventoryService.reserveInventory(id, quantity)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> {
          // Handle specific exceptions if needed
          return Mono.just(ResponseEntity.internalServerError().build());
        });
  }

  @PatchMapping("/{id}/release")
  @ResponseMessage(message = "Release inventory success")
  public Mono<ResponseEntity<InventoryVm>> releaseInventory(
      @PathVariable UUID id,
      @RequestParam Integer quantity) {
    return inventoryService.releaseInventory(id, quantity)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> {
          // Handle specific exceptions if needed
          return Mono.just(ResponseEntity.internalServerError().build());
        });
  }
}
