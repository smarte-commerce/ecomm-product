package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.UpdateInventoryRequest;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.InventoryService;
import com.winnguyen1905.product.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Inventory Management REST API Controller
 * 
 * Simplified inventory operations including querying, updating,
 * reserving and releasing inventory
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inventory Management", description = "Core inventory operations")
public class InventoryController extends BaseController {

  private final InventoryService inventoryService;

  @GetMapping("/product/{productId}")
  @ResponseMessage(message = "Get product inventories success")
  @Operation(summary = "Get product inventories", description = "Retrieves all inventories for a specific product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved product inventories"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<PagedResponse<InventoryVm>> getProductInventories(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      Pageable pageable,
      TAccountRequest accountRequest) {
    logRequest("Getting inventories for product", productId, accountRequest);
    var result = inventoryService.getProductInventory(productId, pageable);
    return ok(result);
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get inventory by id success")
  @Operation(summary = "Get inventory by ID", description = "Retrieves inventory details by its ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<InventoryVm> getInventoryById(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      TAccountRequest accountRequest) {
    logRequest("Getting inventory", id, accountRequest);
    var result = inventoryService.getInventoryById(id);
    return ok(result);
  }

  @GetMapping("/sku/{sku}")
  @ResponseMessage(message = "Get inventory by SKU success")
  @Operation(summary = "Get inventory by SKU", description = "Retrieves inventory details by SKU")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory"),
      @ApiResponse(responseCode = "404", description = "Inventory not found with the given SKU")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'CUSTOMER')")
  public ResponseEntity<InventoryVm> getInventoryBySku(
      @Parameter(description = "Product SKU", required = true) @PathVariable String sku,
      TAccountRequest accountRequest) {
    logRequest("Getting inventory by SKU: " + sku, accountRequest);
    var result = inventoryService.getInventoryBySku(sku);
    return ok(result);
  }

  @PutMapping("/{id}")
  @ResponseMessage(message = "Update inventory success")
  @Operation(summary = "Update inventory", description = "Updates inventory details")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully updated inventory"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "403", description = "Not authorized to update inventory"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<InventoryVm> updateInventory(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody UpdateInventoryRequest request,
      TAccountRequest accountRequest) {
    logRequest("Updating inventory", id, accountRequest);
    // TODO: Update service method to accept UpdateInventoryRequest
    var result = inventoryService.updateInventory(id);
    return ok(result);
  }

  @PatchMapping("/{id}/reserve")
  @ResponseMessage(message = "Reserve inventory success")
  @Operation(summary = "Reserve inventory", description = "Reserves a specific quantity from inventory")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully reserved inventory"),
      @ApiResponse(responseCode = "400", description = "Insufficient quantity or invalid request"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'CUSTOMER')")
  public Mono<ResponseEntity<InventoryVm>> reserveInventory(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      @Parameter(description = "Quantity to reserve", required = true) @RequestParam Integer quantity,
      TAccountRequest accountRequest) {
    logRequest("Reserving inventory " + id + " with quantity " + quantity, accountRequest);
    return inventoryService.reserveInventory(id, quantity)
        .map(this::ok)
        .onErrorResume(e -> {
          log.error("Error reserving inventory {}: {}", id, e.getMessage());
          return Mono.just(ResponseEntity.badRequest().build());
        });
  }

  @PatchMapping("/{id}/release")
  @ResponseMessage(message = "Release inventory success")
  @Operation(summary = "Release inventory", description = "Releases previously reserved inventory")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully released inventory"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "403", description = "Not authorized to release inventory"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public Mono<ResponseEntity<InventoryVm>> releaseInventory(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      @Parameter(description = "Quantity to release", required = true) @RequestParam Integer quantity,
      TAccountRequest accountRequest) {
    logRequest("Releasing inventory " + id + " with quantity " + quantity, accountRequest);
    return inventoryService.releaseInventory(id, quantity)
        .map(this::ok)
        .onErrorResume(e -> {
          log.error("Error releasing inventory {}: {}", id, e.getMessage());
          return Mono.just(ResponseEntity.badRequest().build());
        });
  }

}
