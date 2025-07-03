package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.model.request.InventoryConfirmationRequest;
import com.winnguyen1905.product.core.model.request.ReserveInventoryRequest;
import com.winnguyen1905.product.core.model.request.UpdateInventoryRequest;
import com.winnguyen1905.product.core.model.response.InventoryConfirmationResponse;
import com.winnguyen1905.product.core.model.response.ReserveInventoryResponse;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.CustomerProductService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Inventory Management REST API Controller
 * 
 * Provides endpoints for inventory management including querying, updating,
 * reserving and releasing inventory
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inventory Management", description = "APIs for inventory operations")
public class InventoryController {

  private final InventoryService inventoryService;
  private final CustomerProductService customerProductService;

  @GetMapping("/product/{productId}")
  @ResponseMessage(message = "Get product inventories success")
  @Operation(summary = "Get product inventories", description = "Retrieves all inventories for a specific product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved product inventories"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<PagedResponse<InventoryVm>> getProductInventories(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Getting inventories for product: {} by user: {}", productId, accountRequest.id());
    return ResponseEntity.ok(inventoryService.getProductInventory(productId, pageable));
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get inventory by id success")
  @Operation(summary = "Get inventory by ID", description = "Retrieves inventory details by its ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<InventoryVm> getInventoryById(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      TAccountRequest accountRequest) {
    log.info("Getting inventory: {} by user: {}", id, accountRequest.id());
    return ResponseEntity.ok(inventoryService.getInventoryById(id));
  }

  @GetMapping("/sku/{sku}")
  @ResponseMessage(message = "Get inventory by SKU success")
  @Operation(summary = "Get inventory by SKU", description = "Retrieves inventory details by SKU")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory"),
      @ApiResponse(responseCode = "404", description = "Inventory not found with the given SKU")
  })
  @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'CUSTOMER')")
  public ResponseEntity<InventoryVm> getInventoryBySku(
      @Parameter(description = "Product SKU", required = true) @PathVariable String sku) {
    log.info("Getting inventory by SKU: {}", sku);
    return ResponseEntity.ok(inventoryService.getInventoryBySku(sku));
  }

  @PutMapping("/{id}")
  @ResponseMessage(message = "Update inventory success")
  @Operation(summary = "Update inventory", description = "Updates inventory details")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully updated inventory"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<InventoryVm> updateInventory(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody UpdateInventoryRequest request,
      TAccountRequest accountRequest) {
    log.info("Updating inventory: {} by user: {}", id, accountRequest.id());
    // Note: This is a stub implementation; the service method needs to be updated to accept UpdateInventoryRequest
    return ResponseEntity.ok(inventoryService.updateInventory(id));
  }

  @PatchMapping("/{id}/reserve")
  @ResponseMessage(message = "Reserve inventory success")
  @Operation(summary = "Reserve inventory", description = "Reserves a specific quantity from inventory")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully reserved inventory"),
      @ApiResponse(responseCode = "400", description = "Insufficient quantity or invalid request"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'CUSTOMER')")
  public Mono<ResponseEntity<InventoryVm>> reserveInventory(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      @Parameter(description = "Quantity to reserve", required = true) @RequestParam Integer quantity) {
    log.info("Reserving inventory: {} with quantity: {}", id, quantity);
    return inventoryService.reserveInventory(id, quantity)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> {
          log.error("Error reserving inventory: {}", e.getMessage());
          return Mono.just(ResponseEntity.badRequest().build());
        });
  }

  @PatchMapping("/{id}/release")
  @ResponseMessage(message = "Release inventory success")
  @Operation(summary = "Release inventory", description = "Releases previously reserved inventory")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully released inventory"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public Mono<ResponseEntity<InventoryVm>> releaseInventory(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID id,
      @Parameter(description = "Quantity to release", required = true) @RequestParam Integer quantity,
      TAccountRequest accountRequest) {
    log.info("Releasing inventory: {} with quantity: {} by user: {}", id, quantity, accountRequest.id());
    return inventoryService.releaseInventory(id, quantity)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> {
          log.error("Error releasing inventory: {}", e.getMessage());
          return Mono.just(ResponseEntity.badRequest().build());
        });
  }

  @PostMapping("/check-availability")
  @ResponseMessage(message = "Check inventory availability success")
  @Operation(summary = "Check inventory availability", description = "Checks availability for multiple inventory items")
  public ResponseEntity<InventoryConfirmationResponse> checkInventoryAvailability(
      @Valid @RequestBody InventoryConfirmationRequest request) {
    log.info("Checking inventory availability for reservation: {}", request.getReservationId());
    return ResponseEntity.ok(customerProductService.inventoryConfirmation(request));
  }

  @PostMapping("/reserve-batch")
  @ResponseMessage(message = "Batch reserve inventory success")
  @Operation(summary = "Batch reserve inventory", description = "Reserves multiple inventory items")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<ReserveInventoryResponse> reserveInventoryBatch(
      @Valid @RequestBody ReserveInventoryRequest request,
      TAccountRequest accountRequest) {
    log.info("Batch reserving inventory for user: {}", accountRequest.id());
    return ResponseEntity.ok(customerProductService.reserveInventory(request));
  }
}
