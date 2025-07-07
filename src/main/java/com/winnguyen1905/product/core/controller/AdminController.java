package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin Product Management REST API Controller
 * 
 * Administrative endpoints for product and inventory management
 * All endpoints require admin privileges
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Validated
// @PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Product Management", description = "Administrative APIs for product and inventory management")
public class AdminController extends BaseController {

  private final InventoryService inventoryService;
  private final AdminProductService adminProductService;

  // ================== INVENTORY MANAGEMENT ==================

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
    logRequest("Retrieving all inventories", accountRequest);
    var result = adminProductService.getAllInventories(pageable);
    return ok(result);
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
    logRequest("Retrieving inventory", inventoryId, accountRequest);
    var result = inventoryService.getInventoryById(inventoryId);
    return ok(result);
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
    logRequest("Deleting inventory", inventoryId, accountRequest);
    adminProductService.deleteInventory(inventoryId);
    return noContent();
  }

  // ================== PRODUCT MANAGEMENT ==================

  @GetMapping("/products/pending-approval")
  @ResponseMessage(message = "Get pending products success")
  @Operation(summary = "Get pending products", description = "Admin-only endpoint to retrieve products pending approval")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved pending products"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource")
  })
  public ResponseEntity<PagedResponse<?>> getPendingApprovalProducts(
      Pageable pageable,
      TAccountRequest accountRequest) {
    logRequest("Retrieving pending approval products", accountRequest);
    var result = adminProductService.getPendingApprovalProducts(pageable);
    return ok(result);
  }

  @PatchMapping("/products/{productId}/approve")
  @ResponseMessage(message = "Approve product success")
  @Operation(summary = "Approve product", description = "Admin-only endpoint to approve a product for publishing")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product approved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid approval request"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> approveProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Parameter(description = "Approval status", required = true) @RequestParam boolean approved,
      @Parameter(description = "Rejection reason if not approved") @RequestParam(required = false) String rejectionReason,
      TAccountRequest accountRequest) {
    logRequest("Approving product " + productId + " with status " + approved, accountRequest);
    adminProductService.approveProduct(productId, approved, rejectionReason);
    return noContent();
  }

  // ================== CACHE MANAGEMENT ==================

  @GetMapping("/system/cache/status")
  @ResponseMessage(message = "Get cache status success")
  @Operation(summary = "Get cache status", description = "Admin-only endpoint to retrieve cache statistics")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved cache status"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource")
  })
  public ResponseEntity<Object> getCacheStatus(TAccountRequest accountRequest) {
    logRequest("Retrieving cache status", accountRequest);
    var result = adminProductService.getCacheStatistics();
    return ok(result);
  }

  @PostMapping("/system/cache/clear")
  @ResponseMessage(message = "Clear cache success")
  @Operation(summary = "Clear cache", description = "Admin-only endpoint to clear system caches")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cache cleared successfully"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource")
  })
  public ResponseEntity<Void> clearCache(
      @Parameter(description = "Cache name (optional)") @RequestParam(required = false) String cacheName,
      TAccountRequest accountRequest) {
    logRequest("Clearing cache: " + (cacheName != null ? cacheName : "all"), accountRequest);
    adminProductService.clearCache(cacheName);
    return noContent();
  }
}
