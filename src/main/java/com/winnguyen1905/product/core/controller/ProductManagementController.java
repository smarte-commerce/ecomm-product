package com.winnguyen1905.product.core.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.model.request.CreateProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.EnhancedProductService;
import com.winnguyen1905.product.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Product Management REST API Controller
 * 
 * @deprecated This controller has been split into focused controllers:
 * - {@link ProductCrudController} for basic CRUD operations
 * - {@link ProductSearchController} for search functionality
 * - {@link ProductVendorController} for vendor-specific operations
 * - {@link ProductBulkController} for bulk operations
 * - {@link ProductCacheController} for cache management
 * 
 * This controller will be removed in a future version.
 * Please migrate to the new focused controllers.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Management", description = "Administrative APIs for product management")
@Deprecated(since = "2024-01", forRemoval = true)
public class ProductManagementController {

  private final EnhancedProductService enhancedProductService;

  // ================== PRODUCT CRUD OPERATIONS ==================

  @PostMapping
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Create product success")
  @Operation(summary = "Create new product", description = "Create new product with multi-vendor support")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Product created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  public ResponseEntity<ProductResponse> createProduct(
      @Valid @RequestBody CreateProductRequest request,
      TAccountRequest accountRequest) {
    
    log.info("Creating product for vendor: {}", accountRequest.id());
    
    try {
      ProductResponse response = enhancedProductService.createProduct(request, accountRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
      log.error("Error creating product for vendor {}: {}", accountRequest.id(), e.getMessage());
      throw e;
    }
  }

  @GetMapping("/{productId}")
  @Operation(summary = "Get product details", description = "Get detailed product information")
  public ResponseEntity<ProductResponse> getProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    
    log.info("Getting product: {} for user: {}", productId, 
             accountRequest != null ? accountRequest.id() : "anonymous");
    
    ProductResponse response = enhancedProductService.getProduct(productId, accountRequest);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/public/{productId}")
  @Operation(summary = "Get public product", description = "Get public product information without authentication")
  public ResponseEntity<ProductResponse> getPublicProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    
    log.info("Getting public product: {}", productId);
    
    ProductResponse response = enhancedProductService.getPublicProduct(productId);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{productId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update product success")
  @Operation(summary = "Update product", description = "Update product information with optimistic locking")
  public ResponseEntity<ProductResponse> updateProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Valid @RequestBody UpdateProductRequest request,
      TAccountRequest accountRequest) {
    
    log.info("Updating product: {} for vendor: {}", productId, accountRequest.id());
    
    ProductResponse response = enhancedProductService.updateProduct(productId, request, accountRequest);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{productId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Delete product success")
  @Operation(summary = "Delete product", description = "Soft delete product")
  public ResponseEntity<Void> deleteProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    
    log.info("Deleting product: {} for vendor: {}", productId, accountRequest.id());
    
    enhancedProductService.deleteProduct(productId, accountRequest);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{productId}/restore")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Restore product success")
  @Operation(summary = "Restore product", description = "Restore deleted product")
  public ResponseEntity<ProductResponse> restoreProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    
    log.info("Restoring product: {} for vendor: {}", productId, accountRequest.id());
    
    ProductResponse response = enhancedProductService.restoreProduct(productId, accountRequest);
    return ResponseEntity.ok(response);
  }

  // ================== VENDOR PRODUCT MANAGEMENT ==================

  @GetMapping("/vendor/{vendorId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Get vendor products", description = "Get products list for vendor")
  public ResponseEntity<PagedResponse<ProductResponse>> getVendorProducts(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      @Parameter(description = "Product Status Filter") @RequestParam(required = false) ProductStatus status,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    
    log.info("Getting products for vendor: {} with status: {}", vendorId, status);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getVendorProducts(
        vendorId, status, pageable, accountRequest);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/shop/{shopId}")
  @Operation(summary = "Get shop products", description = "Get public products for shop")
  public ResponseEntity<PagedResponse<ProductResponse>> getShopProducts(
      @Parameter(description = "Shop ID", required = true) @PathVariable UUID shopId,
      @PageableDefault(size = 20) Pageable pageable) {
    
    log.info("Getting public products for shop: {}", shopId);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getShopProducts(shopId, pageable);
    return ResponseEntity.ok(response);
  }

  // ================== PRODUCT SEARCH ==================

  @PostMapping("/search")
  @Operation(summary = "Search products", description = "Search products with Elasticsearch")
  public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
      @Valid @RequestBody SearchProductRequest request,
      TAccountRequest accountRequest) {
    
    log.info("Searching products for user: {}", 
             accountRequest != null ? accountRequest.id() : "anonymous");
    
    PagedResponse<ProductResponse> response = enhancedProductService.searchProducts(request, accountRequest);
    return ResponseEntity.ok(response);
  }

  // ================== BULK OPERATIONS ==================

  @PatchMapping("/bulk/status")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update products status success")
  @Operation(summary = "Bulk update product status", description = "Bulk update product status")
  public ResponseEntity<Void> updateProductsStatus(
      @RequestBody List<UUID> productIds,
      @RequestParam ProductStatus status,
      TAccountRequest accountRequest) {
    
    log.info("Bulk updating {} products status to {} for vendor: {}", 
             productIds.size(), status, accountRequest.id());
    
    enhancedProductService.updateProductsStatus(productIds, status, accountRequest);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/bulk/publish")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update publish status success")
  @Operation(summary = "Bulk publish/unpublish products", description = "Bulk publish/unpublish products")
  public ResponseEntity<Void> updateProductsPublishStatus(
      @RequestBody List<UUID> productIds,
      @RequestParam Boolean published,
      TAccountRequest accountRequest) {
    
    log.info("Bulk updating {} products publish status to {} for vendor: {}", 
             productIds.size(), published, accountRequest.id());
    
    enhancedProductService.updateProductsPublishStatus(productIds, published, accountRequest);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/bulk")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Delete products success")
  @Operation(summary = "Bulk delete products", description = "Bulk delete products")
  public ResponseEntity<Void> deleteProducts(
      @RequestBody List<UUID> productIds,
      TAccountRequest accountRequest) {
    
    log.info("Bulk deleting {} products for vendor: {}", productIds.size(), accountRequest.id());
    
    enhancedProductService.deleteProducts(productIds, accountRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/bulk/import")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Import products success")
  @Operation(summary = "Bulk import products", description = "Bulk import products")
  public ResponseEntity<List<ProductResponse>> bulkImportProducts(
      @Valid @RequestBody List<CreateProductRequest> requests,
      TAccountRequest accountRequest) {
    
    log.info("Bulk importing {} products for vendor: {}", requests.size(), accountRequest.id());
    
    List<ProductResponse> responses = enhancedProductService.bulkImportProducts(requests, accountRequest);
    return ResponseEntity.ok(responses);
  }

  // ================== CATEGORY & BRAND FILTERING ==================

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get products by category", description = "Filter products by category")
  public ResponseEntity<PagedResponse<ProductResponse>> getProductsByCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID categoryId,
      @Parameter(description = "Vendor ID for filtering") @RequestParam(required = false) UUID vendorId,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    
    log.info("Getting products by category: {} for vendor: {}", categoryId, vendorId);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getProductsByCategory(
        categoryId, vendorId, pageable, accountRequest);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/brand/{brandId}")
  @Operation(summary = "Get products by brand", description = "Filter products by brand")
  public ResponseEntity<PagedResponse<ProductResponse>> getProductsByBrand(
      @Parameter(description = "Brand ID", required = true) @PathVariable UUID brandId,
      @Parameter(description = "Vendor ID for filtering") @RequestParam(required = false) UUID vendorId,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    
    log.info("Getting products by brand: {} for vendor: {}", brandId, vendorId);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getProductsByBrand(
        brandId, vendorId, pageable, accountRequest);
    return ResponseEntity.ok(response);
  }

  // ================== SEO & SLUG OPERATIONS ==================

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Get product by slug", description = "Get product by SEO-friendly slug")
  public ResponseEntity<ProductResponse> getProductBySlug(
      @Parameter(description = "Product slug", required = true) @PathVariable String slug,
      @Parameter(description = "Vendor ID") @RequestParam(required = false) UUID vendorId) {
    
    log.info("Getting product by slug: {} for vendor: {}", slug, vendorId);
    
    ProductResponse response = enhancedProductService.getProductBySlug(slug, vendorId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{productId}/seo")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update SEO success")
  @Operation(summary = "Update SEO metadata", description = "Update product SEO metadata")
  public ResponseEntity<ProductResponse> updateProductSEO(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @RequestParam(required = false) String metaTitle,
      @RequestParam(required = false) String metaDescription,
      @RequestParam(required = false) String metaKeywords,
      TAccountRequest accountRequest) {
    
    log.info("Updating SEO for product: {} by vendor: {}", productId, accountRequest.id());
    
    ProductResponse response = enhancedProductService.updateProductSEO(
        productId, metaTitle, metaDescription, metaKeywords, accountRequest);
    return ResponseEntity.ok(response);
  }

  // ================== ANALYTICS & STATISTICS ==================

  @GetMapping("/analytics/vendor/{vendorId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Get vendor product statistics", description = "Get vendor product statistics")
  public ResponseEntity<EnhancedProductService.VendorProductStats> getVendorProductStats(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      TAccountRequest accountRequest) {
    
    log.info("Getting product stats for vendor: {}", vendorId);
    
    EnhancedProductService.VendorProductStats stats = enhancedProductService.getVendorProductStats(
        vendorId, accountRequest);
    return ResponseEntity.ok(stats);
  }

  @GetMapping("/popular")
  @Operation(summary = "Get popular products", description = "Get popular products based on purchases")
  public ResponseEntity<PagedResponse<ProductResponse>> getPopularProducts(
      @Parameter(description = "Minimum purchases threshold") @RequestParam(defaultValue = "10") Long minPurchases,
      @PageableDefault(size = 20) Pageable pageable) {
    
    log.info("Getting popular products with min purchases: {}", minPurchases);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getPopularProducts(minPurchases, pageable);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{productId}/related")
  @Operation(summary = "Get related products", description = "Get related products")
  public ResponseEntity<PagedResponse<ProductResponse>> getRelatedProducts(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @PageableDefault(size = 10) Pageable pageable) {
    
    log.info("Getting related products for: {}", productId);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getRelatedProducts(productId, pageable);
    return ResponseEntity.ok(response);
  }

  // ================== CACHE MANAGEMENT ==================

  @PostMapping("/{productId}/cache/evict")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Evict cache success")
  @Operation(summary = "Evict product cache", description = "Evict product cache")
  public ResponseEntity<Void> evictProductCache(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    
    log.info("Evicting cache for product: {}", productId);
    
    enhancedProductService.evictProductCache(productId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/cache/warmup")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Warm up cache success")
  @Operation(summary = "Warm up cache", description = "Warm up product cache")
  public ResponseEntity<Void> warmUpProductCache() {
    
    log.info("Warming up product cache");
    
    enhancedProductService.warmUpProductCache();
    return ResponseEntity.ok().build();
  }

  // ================== INVENTORY SYNC ==================

  @PostMapping("/{productId}/sync-inventory")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Sync inventory success")
  @Operation(summary = "Sync inventory", description = "Sync product inventory with Elasticsearch")
  public ResponseEntity<Void> syncProductInventory(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    
    log.info("Syncing inventory for product: {}", productId);
    
    enhancedProductService.syncProductInventory(productId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/low-stock")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Get low stock products", description = "Get low stock products")
  public ResponseEntity<List<ProductResponse>> checkLowStockProducts(
      @Parameter(description = "Vendor ID") @RequestParam UUID vendorId) {
    
    log.info("Checking low stock products for vendor: {}", vendorId);
    
    List<ProductResponse> response = enhancedProductService.checkLowStockProducts(vendorId);
    return ResponseEntity.ok(response);
  }
}
