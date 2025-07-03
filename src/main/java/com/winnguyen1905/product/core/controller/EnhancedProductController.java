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
import com.winnguyen1905.product.core.model.viewmodel.RestResponse;
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
 * Enhanced Product Management REST API Controller
 * 
 * Comprehensive API endpoints for multi-vendor product management
 * Includes full CRUD operations, search, analytics, and bulk operations
 * 
 * This controller replaces the legacy VendorProductService.addProduct() method
 * with the more comprehensive createProduct() method that includes:
 * - Better validation and error handling
 * - Automatic Elasticsearch indexing
 * - Enhanced caching support
 * - More comprehensive request/response models
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Management", description = "Comprehensive Product Management APIs")
public class EnhancedProductController {

  private final EnhancedProductService enhancedProductService;

  // ================== BASIC CRUD OPERATIONS ==================

  @PostMapping
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Tạo sản phẩm thành công")
  @Operation(summary = "Tạo sản phẩm mới", description = "Tạo sản phẩm mới với multi-vendor support")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Tạo sản phẩm thành công"),
      @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
      @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
  })
  public ResponseEntity<ProductResponse> createProduct(
      @Valid @RequestBody CreateProductRequest request,
      TAccountRequest accountRequest) {

    log.info("Creating product: {} for vendor: {}", request.name(), accountRequest.id());

    ProductResponse response = enhancedProductService.createProduct(request, accountRequest);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body((response));
  }

  @GetMapping("/{productId}")
  @Operation(summary = "Lấy chi tiết sản phẩm", description = "Lấy thông tin chi tiết của sản phẩm")
  public ResponseEntity<ProductResponse> getProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {

    ProductResponse response = enhancedProductService.getProduct(productId, accountRequest);

    // Increment view count asynchronously
    enhancedProductService.incrementProductView(productId);

    return ResponseEntity.ok((response));
  }

  @GetMapping("/public/{productId}")
  @Operation(summary = "Lấy sản phẩm công khai", description = "Lấy thông tin sản phẩm công khai không cần authentication")
  public ResponseEntity<ProductResponse> getPublicProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {

    ProductResponse response = enhancedProductService.getPublicProduct(productId);

    // Increment view count asynchronously
    enhancedProductService.incrementProductView(productId);

    return ResponseEntity.ok((response));
  }

  @PutMapping("/{productId}")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Cập nhật sản phẩm thành công")
  @Operation(summary = "Cập nhật sản phẩm", description = "Cập nhật thông tin sản phẩm với optimistic locking")
  public ResponseEntity<ProductResponse> updateProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Valid @RequestBody UpdateProductRequest request,
      TAccountRequest accountRequest) {

    log.info("Updating product: {} by vendor: {}", productId, accountRequest.id());

    ProductResponse response = enhancedProductService.updateProduct(productId, request, accountRequest);

    return ResponseEntity.ok((response));
  }

  @DeleteMapping("/{productId}")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Xóa sản phẩm thành công")
  @Operation(summary = "Xóa sản phẩm", description = "Xóa mềm sản phẩm (soft delete)")
  public ResponseEntity<Void> deleteProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {

    log.info("Deleting product: {} by vendor: {}", productId, accountRequest.id());

    enhancedProductService.deleteProduct(productId, accountRequest);

    return ResponseEntity.ok((null));
  }

  @PatchMapping("/{productId}/restore")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Khôi phục sản phẩm thành công")
  @Operation(summary = "Khôi phục sản phẩm", description = "Khôi phục sản phẩm đã xóa")
  public ResponseEntity<ProductResponse> restoreProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {

    ProductResponse response = enhancedProductService.restoreProduct(productId, accountRequest);

    return ResponseEntity.ok((response));
  }

  // ================== VENDOR OPERATIONS ==================

  @GetMapping("/vendor/{vendorId}")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Lấy sản phẩm theo vendor", description = "Lấy danh sách sản phẩm của vendor")
  public ResponseEntity<PagedResponse<ProductResponse>> getVendorProducts(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      @Parameter(description = "Product Status Filter") @RequestParam(required = false) ProductStatus status,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {

    PagedResponse<ProductResponse> response = enhancedProductService
        .getVendorProducts(vendorId, status, pageable, accountRequest);

    return ResponseEntity.ok((response));
  }

  @GetMapping("/shop/{shopId}")
  @Operation(summary = "Lấy sản phẩm theo shop", description = "Lấy danh sách sản phẩm công khai của shop")
  public ResponseEntity<PagedResponse<ProductResponse>> getShopProducts(
      @Parameter(description = "Shop ID", required = true) @PathVariable UUID shopId,
      @PageableDefault(size = 20) Pageable pageable) {

    PagedResponse<ProductResponse> response = enhancedProductService
        .getShopProducts(shopId, pageable);

    return ResponseEntity.ok((response));
  }

  @PostMapping("/search")
  @Operation(summary = "Tìm kiếm sản phẩm", description = "Tìm kiếm sản phẩm với Elasticsearch")
  public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
      @Valid @RequestBody SearchProductRequest request,
      TAccountRequest accountRequest) {

    PagedResponse<ProductResponse> response = enhancedProductService
        .searchProducts(request, accountRequest);

    return ResponseEntity.ok((response));
  }

  // ================== BULK OPERATIONS ==================

  @PatchMapping("/bulk/status")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Cập nhật trạng thái sản phẩm thành công")
  @Operation(summary = "Cập nhật trạng thái nhiều sản phẩm", description = "Bulk update product status")
  public ResponseEntity<Void> updateProductsStatus(
      @RequestBody List<UUID> productIds,
      @RequestParam ProductStatus status,
      TAccountRequest accountRequest) {

    log.info("Bulk updating status for {} products to {} by vendor: {}",
        productIds.size(), status, accountRequest.id());

    enhancedProductService.updateProductsStatus(productIds, status, accountRequest);

    return ResponseEntity.ok((null));
  }

  @PatchMapping("/bulk/publish")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Cập nhật trạng thái published thành công")
  @Operation(summary = "Publish/Unpublish nhiều sản phẩm", description = "Bulk publish/unpublish products")
  public ResponseEntity<Void> updateProductsPublishStatus(
      @RequestBody List<UUID> productIds,
      @RequestParam Boolean published,
      TAccountRequest accountRequest) {

    enhancedProductService.updateProductsPublishStatus(productIds, published, accountRequest);

    return ResponseEntity.ok((null));
  }

  @DeleteMapping("/bulk")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Xóa nhiều sản phẩm thành công")
  @Operation(summary = "Xóa nhiều sản phẩm", description = "Bulk delete products")
  public ResponseEntity<Void> deleteProducts(
      @RequestBody List<UUID> productIds,
      TAccountRequest accountRequest) {

    enhancedProductService.deleteProducts(productIds, accountRequest);

    return ResponseEntity.ok((null));
  }

  @PostMapping("/bulk/import")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Import sản phẩm thành công")
  @Operation(summary = "Import nhiều sản phẩm", description = "Bulk import products")
  public ResponseEntity<List<ProductResponse>> bulkImportProducts(
      @Valid @RequestBody List<CreateProductRequest> requests,
      TAccountRequest accountRequest) {

    List<ProductResponse> responses = enhancedProductService.bulkImportProducts(requests, accountRequest);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body((responses));
  }

  // ================== CATEGORY & BRAND FILTERING ==================

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Lấy sản phẩm theo category", description = "Filter products by category")
  public ResponseEntity<PagedResponse<ProductResponse>> getProductsByCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID categoryId,
      @Parameter(description = "Vendor ID for filtering") @RequestParam(required = false) UUID vendorId,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {

    PagedResponse<ProductResponse> response = enhancedProductService
        .getProductsByCategory(categoryId, vendorId, pageable, accountRequest);

    return ResponseEntity.ok((response));
  }

  @GetMapping("/brand/{brandId}")
  @Operation(summary = "Lấy sản phẩm theo brand", description = "Filter products by brand")
  public ResponseEntity<PagedResponse<ProductResponse>> getProductsByBrand(
      @Parameter(description = "Brand ID", required = true) @PathVariable UUID brandId,
      @Parameter(description = "Vendor ID for filtering") @RequestParam(required = false) UUID vendorId,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {

    PagedResponse<ProductResponse> response = enhancedProductService
        .getProductsByBrand(brandId, vendorId, pageable, accountRequest);

    return ResponseEntity.ok((response));
  }

  // ================== SEO & SLUG ==================

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Lấy sản phẩm theo slug", description = "Get product by SEO-friendly slug")
  public ResponseEntity<ProductResponse> getProductBySlug(
      @Parameter(description = "Product slug", required = true) @PathVariable String slug,
      @Parameter(description = "Vendor ID") @RequestParam(required = false) UUID vendorId) {

    ProductResponse response = enhancedProductService.getProductBySlug(slug, vendorId);

    // Increment view count asynchronously
    enhancedProductService.incrementProductView(response.id());

    return ResponseEntity.ok((response));
  }

  @PatchMapping("/{productId}/seo")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Cập nhật SEO thành công")
  @Operation(summary = "Cập nhật SEO metadata", description = "Update product SEO metadata")
  public ResponseEntity<ProductResponse> updateProductSEO(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @RequestParam(required = false) String metaTitle,
      @RequestParam(required = false) String metaDescription,
      @RequestParam(required = false) String metaKeywords,
      TAccountRequest accountRequest) {

    ProductResponse response = enhancedProductService
        .updateProductSEO(productId, metaTitle, metaDescription, metaKeywords, accountRequest);

    return ResponseEntity.ok((response));
  }

  // ================== ANALYTICS & REPORTING ==================

  @GetMapping("/analytics/vendor/{vendorId}")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Thống kê sản phẩm vendor", description = "Get vendor product statistics")
  public ResponseEntity<EnhancedProductService.VendorProductStats> getVendorProductStats(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      TAccountRequest accountRequest) {

    EnhancedProductService.VendorProductStats stats = enhancedProductService
        .getVendorProductStats(vendorId, accountRequest);

    return ResponseEntity.ok((stats));
  }

  @GetMapping("/popular")
  @Operation(summary = "Sản phẩm phổ biến", description = "Get popular products based on purchases")
  public ResponseEntity<PagedResponse<ProductResponse>> getPopularProducts(
      @Parameter(description = "Minimum purchases threshold") @RequestParam(defaultValue = "10") Long minPurchases,
      @PageableDefault(size = 20) Pageable pageable) {

    PagedResponse<ProductResponse> response = enhancedProductService
        .getPopularProducts(minPurchases, pageable);

    return ResponseEntity.ok((response));
  }

  @GetMapping("/{productId}/related")
  @Operation(summary = "Sản phẩm liên quan", description = "Get related products")
  public ResponseEntity<PagedResponse<ProductResponse>> getRelatedProducts(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @PageableDefault(size = 10) Pageable pageable) {

    PagedResponse<ProductResponse> response = enhancedProductService
        .getRelatedProducts(productId, pageable);

    return ResponseEntity.ok((response));
  }

  // ================== CACHE MANAGEMENT ==================

  @PostMapping("/{productId}/cache/evict")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Xóa cache thành công")
  @Operation(summary = "Xóa cache sản phẩm", description = "Evict product cache")
  public ResponseEntity<Void> evictProductCache(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {

    enhancedProductService.evictProductCache(productId);

    return ResponseEntity.ok((null));
  }

  @PostMapping("/cache/warmup")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Warm up cache thành công")
  @Operation(summary = "Warm up cache", description = "Warm up product cache")
  public ResponseEntity<Void> warmUpProductCache() {

    enhancedProductService.warmUpProductCache();

    return ResponseEntity.ok((null));
  }

  // ================== INVENTORY OPERATIONS ==================

  @PostMapping("/{productId}/sync-inventory")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Đồng bộ inventory thành công")
  @Operation(summary = "Đồng bộ inventory", description = "Sync product inventory with Elasticsearch")
  public ResponseEntity<Void> syncProductInventory(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {

    enhancedProductService.syncProductInventory(productId);

    return ResponseEntity.ok((null));
  }

  @GetMapping("/low-stock")
  @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @Operation(summary = "Sản phẩm sắp hết hàng", description = "Get low stock products")
  public ResponseEntity<List<ProductResponse>> checkLowStockProducts(
      @Parameter(description = "Vendor ID") @RequestParam UUID vendorId) {

    List<ProductResponse> response = enhancedProductService.checkLowStockProducts(vendorId);

    return ResponseEntity.ok((response));
  }
}
