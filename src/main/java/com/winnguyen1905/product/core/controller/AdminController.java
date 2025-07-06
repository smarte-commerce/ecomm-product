package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.AdminProductService;
import com.winnguyen1905.product.core.service.InventoryService;
import com.winnguyen1905.product.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

/**
 * Admin Product Management REST API Controller
 * 
 * Administrative endpoints for product, inventory, and vendor management
 * All endpoints require admin privileges
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Management", description = "Administrative APIs for system management")
public class AdminController {

  private final InventoryService inventoryService;
  private final AdminProductService adminProductService;

  @GetMapping("/inventories")
  @ResponseMessage(message = "Get all inventories success")
  @Operation(summary = "Get all inventories", description = "Admin-only endpoint to retrieve all inventories")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved inventories"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource")
  })
  public ResponseEntity<PagedResponse<InventoryVm>> getAllInventories(
      Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Admin {} retrieving all inventories", accountRequest.id());
    return ResponseEntity.ok(adminProductService.getAllInventories(pageable));
  }

  @GetMapping("/inventories/{inventoryId}")
  @ResponseMessage(message = "Get inventory by ID success")
  @Operation(summary = "Get inventory by ID", description = "Admin-only endpoint to get inventory details")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  public ResponseEntity<InventoryVm> getInventoryById(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID inventoryId,
      TAccountRequest accountRequest) {
    log.info("Admin {} retrieving inventory {}", accountRequest.id(), inventoryId);
    return ResponseEntity.ok(inventoryService.getInventoryById(inventoryId));
  }

  @DeleteMapping("/inventories/{inventoryId}")
  @ResponseMessage(message = "Delete inventory success")
  @Operation(summary = "Delete inventory", description = "Admin-only endpoint to delete inventory")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully deleted inventory"),
      @ApiResponse(responseCode = "404", description = "Inventory not found")
  })
  public ResponseEntity<Void> deleteInventory(
      @Parameter(description = "Inventory ID", required = true) @PathVariable UUID inventoryId,
      TAccountRequest accountRequest) {
    log.info("Admin {} deleting inventory {}", accountRequest.id(), inventoryId);
    adminProductService.deleteInventory(inventoryId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/products/pending-approval")
  @ResponseMessage(message = "Get pending products success")
  @Operation(summary = "Get pending products", description = "Admin-only endpoint to retrieve products pending approval")
  public ResponseEntity<PagedResponse<?>> getPendingApprovalProducts(
      Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Admin {} retrieving pending approval products", accountRequest.id());
    return ResponseEntity.ok(adminProductService.getPendingApprovalProducts(pageable));
  }

  @PatchMapping("/products/{productId}/approve")
  @ResponseMessage(message = "Approve product success")
  @Operation(summary = "Approve product", description = "Admin-only endpoint to approve a product for publishing")
  public ResponseEntity<Void> approveProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @RequestBody ProductApprovalRequest request,
      TAccountRequest accountRequest) {
    log.info("Admin {} approving product {}", accountRequest.id(), productId);
    adminProductService.approveProduct(productId, request.isPublished(), request.rejectionReason());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/system/cache/status")
  @ResponseMessage(message = "Get cache status success")
  @Operation(summary = "Get cache status", description = "Admin-only endpoint to retrieve cache statistics")
  public ResponseEntity<Object> getCacheStatus(TAccountRequest accountRequest) {
    log.info("Admin {} retrieving cache status", accountRequest.id());
    return ResponseEntity.ok(adminProductService.getCacheStatistics());
  }

  @PostMapping("/system/cache/clear")
  @ResponseMessage(message = "Clear cache success")
  @Operation(summary = "Clear cache", description = "Admin-only endpoint to clear system caches")
  public ResponseEntity<Void> clearCache(
      @Parameter(description = "Cache name (optional)") @RequestParam(required = false) String cacheName,
      TAccountRequest accountRequest) {
    log.info("Admin {} clearing cache: {}", accountRequest.id(), cacheName != null ? cacheName : "all");
    adminProductService.clearCache(cacheName);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/vendors/performance")
  @ResponseMessage(message = "Get vendor performance success")
  @Operation(summary = "Get vendor performance", description = "Admin-only endpoint to retrieve vendor performance metrics")
  public ResponseEntity<List<?>> getVendorPerformance(
      @Parameter(description = "Days to analyze") @RequestParam(defaultValue = "30") Integer days,
      TAccountRequest accountRequest) {
    log.info("Admin {} retrieving vendor performance for the past {} days", accountRequest.id(), days);
    return ResponseEntity.ok(adminProductService.getVendorPerformance(days));
  }
}
