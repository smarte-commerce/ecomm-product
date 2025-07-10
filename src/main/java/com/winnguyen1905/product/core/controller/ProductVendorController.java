package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.EnhancedProductService;
import com.winnguyen1905.product.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Product Vendor Management REST API Controller
 * 
 * Handles vendor-specific product operations and shop management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/vendor")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Vendor Management", description = "Vendor-specific product operations")
public class ProductVendorController extends BaseController {

  private final EnhancedProductService enhancedProductService;

  @GetMapping("/{vendorId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Get vendor products", description = "Get products list for specific vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only access own products"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getVendorProducts(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      @Parameter(description = "Product Status Filter") @RequestParam(required = false) ProductStatus status,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    
    logRequest("Getting products for vendor " + vendorId + " with status " + status, accountRequest);
    
    // Validate vendor access for non-admin users
    if (!isAdmin(accountRequest)) {
      validateVendorAccess(vendorId, accountRequest);
    }
    
    PagedResponse<ProductResponse> response = enhancedProductService.getVendorProducts(
        vendorId, status, pageable, accountRequest);
    return ok(response);
  }

  @GetMapping("/shop/{shopId}")
  @Operation(summary = "Get shop products", description = "Get public products for shop display")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Shop products retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Shop not found")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getShopProducts(
      @Parameter(description = "Shop ID", required = true) @PathVariable UUID shopId,
      @PageableDefault(size = 20) Pageable pageable) {
    
    logPublicRequest("Getting public products for shop", shopId);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getShopProducts(shopId, pageable);
    return ok(response);
  }

  @GetMapping("/analytics/{vendorId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Get vendor product statistics", description = "Get comprehensive product statistics for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only access own statistics"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  public ResponseEntity<EnhancedProductService.VendorProductStats> getVendorProductStats(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      TAccountRequest accountRequest) {
    
    logRequest("Getting product statistics for vendor", vendorId, accountRequest);
    
    // Validate vendor access for non-admin users
    if (!isAdmin(accountRequest)) {
      validateVendorAccess(vendorId, accountRequest);
    }
    
    EnhancedProductService.VendorProductStats response = enhancedProductService.getVendorProductStats(vendorId, accountRequest);
    return ok(response);
  }

  @GetMapping("/low-stock")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Get low stock products", description = "Get products with low stock levels for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  public ResponseEntity<List<ProductResponse>> checkLowStockProducts(
      @Parameter(description = "Vendor ID", required = true) @RequestParam UUID vendorId,
      TAccountRequest accountRequest) {
    
    logRequest("Getting low stock products for vendor", vendorId, accountRequest);
    
    // Validate vendor access for non-admin users
    if (!isAdmin(accountRequest)) {
      validateVendorAccess(vendorId, accountRequest);
    }
    
    List<ProductResponse> response = enhancedProductService.checkLowStockProducts(vendorId);
    return ok(response);
  }

  @PatchMapping("/{productId}/seo")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update SEO success")
  @Operation(summary = "Update SEO metadata", description = "Update product SEO metadata for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "SEO metadata updated successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only update own products"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> updateProductSEO(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Parameter(description = "Meta title") @RequestParam(required = false) String metaTitle,
      @Parameter(description = "Meta description") @RequestParam(required = false) String metaDescription,
      @Parameter(description = "Meta keywords") @RequestParam(required = false) String metaKeywords,
      TAccountRequest accountRequest) {
    
    logRequest("Updating SEO metadata for product", productId, accountRequest);
    
    ProductResponse response = enhancedProductService.updateProductSEO(
        productId, metaTitle, metaDescription, metaKeywords, accountRequest);
    return ok(response);
  }

  @PostMapping("/{productId}/sync-inventory")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Sync inventory success")
  @Operation(summary = "Sync inventory", description = "Sync product inventory with Elasticsearch for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Inventory synchronized successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only sync own products"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> syncProductInventory(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    
    logRequest("Syncing inventory for product", productId, accountRequest);
    
    enhancedProductService.syncProductInventory(productId);
    return noContent();
  }
} 
