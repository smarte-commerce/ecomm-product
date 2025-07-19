package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.CreateProductRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.AdminProductService;
import com.winnguyen1905.product.core.service.EnhancedProductService;
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

import java.util.List;
import java.util.UUID;

/**
 * Administrative Operations REST API Controller
 * 
 * All administrative operations including product management, inventory,
 * bulk operations, cache management, and system operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin Operations", description = "Administrative APIs for system management")
public class AdminController extends BaseController {

  private final InventoryService inventoryService;
  private final AdminProductService adminProductService;
  private final EnhancedProductService enhancedProductService;

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
  @Operation(summary = "Clear cache", description = "Clear system caches")
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

  @PostMapping("/system/cache/warmup")
  @ResponseMessage(message = "Warm up cache success")
  @Operation(summary = "Warm up cache", description = "Warm up product cache by loading frequently accessed products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cache warmed up successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only")
  })
  public ResponseEntity<Void> warmUpProductCache(TAccountRequest accountRequest) {
    logRequest("Warming up product cache", accountRequest);
    enhancedProductService.warmUpProductCache();
    return noContent();
  }

  @PostMapping("/products/{productId}/cache/evict")
  @ResponseMessage(message = "Evict cache success")
  @Operation(summary = "Evict product cache", description = "Evict specific product from cache")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> evictProductCache(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Evicting cache for product", productId, accountRequest);
    enhancedProductService.evictProductCache(productId);
    return noContent();
  }

  // ================== BULK OPERATIONS ==================

  @PatchMapping("/products/bulk/status")
  @ResponseMessage(message = "Update products status success")
  @Operation(summary = "Bulk update product status", description = "Update status for multiple products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product statuses updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product IDs"),
      @ApiResponse(responseCode = "404", description = "One or more products not found")
  })
  public ResponseEntity<Void> updateProductsStatus(
      @Parameter(description = "List of product IDs", required = true) @RequestBody List<UUID> productIds,
      @Parameter(description = "New status", required = true) @RequestParam ProductStatus status,
      TAccountRequest accountRequest) {
    logRequest("Bulk updating status for " + productIds.size() + " products to " + status, accountRequest);
    enhancedProductService.updateProductsStatus(productIds, status, accountRequest);
    return noContent();
  }

  @PatchMapping("/products/bulk/publish")
  @ResponseMessage(message = "Update publish status success")
  @Operation(summary = "Bulk publish/unpublish products", description = "Publish or unpublish multiple products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product publish status updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product IDs"),
      @ApiResponse(responseCode = "404", description = "One or more products not found")
  })
  public ResponseEntity<Void> updateProductsPublishStatus(
      @Parameter(description = "List of product IDs", required = true) @RequestBody List<UUID> productIds,
      @Parameter(description = "Publish status", required = true) @RequestParam Boolean published,
      TAccountRequest accountRequest) {
    logRequest("Bulk " + (published ? "publishing" : "unpublishing") + " " + productIds.size() + " products", accountRequest);
    enhancedProductService.updateProductsPublishStatus(productIds, published, accountRequest);
    return noContent();
  }

  @DeleteMapping("/products/bulk")
  @ResponseMessage(message = "Delete products success")
  @Operation(summary = "Bulk delete products", description = "Soft delete multiple products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products deleted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product IDs"),
      @ApiResponse(responseCode = "404", description = "One or more products not found")
  })
  public ResponseEntity<Void> deleteProducts(
      @Parameter(description = "List of product IDs", required = true) @RequestBody List<UUID> productIds,
      TAccountRequest accountRequest) {
    logRequest("Bulk deleting " + productIds.size() + " products", accountRequest);
    enhancedProductService.deleteProducts(productIds, accountRequest);
    return noContent();
  }

  @PostMapping("/products/bulk/import")
  @ResponseMessage(message = "Import products success")
  @Operation(summary = "Bulk import products", description = "Import multiple products from a list")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products imported successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product data"),
      @ApiResponse(responseCode = "422", description = "Validation failed for one or more products")
  })
  public ResponseEntity<List<ProductResponse>> bulkImportProducts(
      @Parameter(description = "List of product requests", required = true) @Valid @RequestBody List<CreateProductRequest> requests,
      TAccountRequest accountRequest) {
    logRequest("Bulk importing " + requests.size() + " products", accountRequest);
    List<ProductResponse> responses = enhancedProductService.bulkImportProducts(requests, accountRequest);
    return ok(responses);
  }
}
