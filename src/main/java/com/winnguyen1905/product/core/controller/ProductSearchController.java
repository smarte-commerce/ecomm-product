package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
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

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Product Search REST API Controller
 * 
 * Handles product search operations with Elasticsearch
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/search")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Search", description = "Product search operations")
public class ProductSearchController extends BaseController {

  private final EnhancedProductService enhancedProductService;

  @PostMapping
  @Operation(summary = "Search products", description = "Search products with Elasticsearch using partition-first approach")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Search completed successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid search request")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
      @Valid @RequestBody SearchProductRequest request,
      TAccountRequest accountRequest) {
    
    logRequest("Searching products with partition-first", accountRequest);
    
    // Enhance request with user's region for partition-first search
    SearchProductRequest enhancedRequest = enhanceRequestWithRegion(request, accountRequest);
    
    PagedResponse<ProductResponse> response = enhancedProductService.searchProducts(enhancedRequest, accountRequest);
    return ok(response);
  }

  @PostMapping("/global")
  @Operation(summary = "Search products globally", description = "Search products across all partitions without partition-first optimization")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Global search completed successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid search request")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> searchProductsGlobal(
      @Valid @RequestBody SearchProductRequest request,
      TAccountRequest accountRequest) {
    
    logRequest("Global product search (no partition-first)", accountRequest);
    
    // Disable partition-first for global search
    SearchProductRequest globalRequest = SearchProductRequest.builder()
        .sorts(request.sorts())
        .searchTerm(request.searchTerm())
        .keyword(request.keyword())
        .filters(request.filters())
        .pagination(request.pagination())
        .region(request.region())
        .productType(request.productType())
        .status(request.status())
        .isPublished(request.isPublished())
        .enablePartitionFirst(false) // Explicitly disable
        .build();
    
    PagedResponse<ProductResponse> response = enhancedProductService.searchProducts(globalRequest, accountRequest);
    return ok(response);
  }

  /**
   * Enhance search request with user's region for partition-first search
   */
  private SearchProductRequest enhanceRequestWithRegion(SearchProductRequest request, TAccountRequest accountRequest) {
    // Use the region from request if provided, otherwise use user's detected region
    var effectiveRegion = request.region() != null ? request.region() : accountRequest.region();
    
    return SearchProductRequest.builder()
        .sorts(request.sorts())
        .searchTerm(request.searchTerm())
        .keyword(request.keyword())
        .filters(request.filters())
        .pagination(request.pagination())
        .region(effectiveRegion)
        .productType(request.productType())
        .status(request.status())
        .isPublished(request.isPublished())
        // Set partition-first defaults if not specified
        .enablePartitionFirst(request.enablePartitionFirst() != null ? request.enablePartitionFirst() : true)
        .partitionFirstThreshold(request.partitionFirstThreshold() != null ? request.partitionFirstThreshold() : 0.3)
        .maxResultsFromOtherPartitions(request.maxResultsFromOtherPartitions() != null ? 
                                     request.maxResultsFromOtherPartitions() : 10)
        .includeGlobalProducts(request.includeGlobalProducts() != null ? request.includeGlobalProducts() : true)
        .build();
  }

  @GetMapping("/popular")
  @Operation(summary = "Get popular products", description = "Get popular products based on purchases")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Popular products retrieved successfully")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getPopularProducts(
      @Parameter(description = "Minimum purchases threshold") @RequestParam(defaultValue = "10") Long minPurchases,
      @PageableDefault(size = 20) Pageable pageable) {
    
    logPublicRequest("Getting popular products with min purchases: " + minPurchases);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getPopularProducts(minPurchases, pageable);
    return ok(response);
  }

  @GetMapping("/related/{productId}")
  @Operation(summary = "Get related products", description = "Get products related to the specified product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Related products retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getRelatedProducts(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @PageableDefault(size = 10) Pageable pageable) {
    
    logPublicRequest("Getting related products for product", productId);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getRelatedProducts(productId, pageable);
    return ok(response);
  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get products by category", description = "Filter products by category")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getProductsByCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID categoryId,
      @Parameter(description = "Vendor ID for filtering") @RequestParam(required = false) UUID vendorId,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    
    logRequest("Getting products by category " + categoryId + " for vendor " + vendorId, accountRequest);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getProductsByCategory(
        categoryId, vendorId, pageable, accountRequest);
    return ok(response);
  }

  @GetMapping("/brand/{brandId}")
  @Operation(summary = "Get products by brand", description = "Filter products by brand")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Brand not found")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getProductsByBrand(
      @Parameter(description = "Brand ID", required = true) @PathVariable UUID brandId,
      @Parameter(description = "Vendor ID for filtering") @RequestParam(required = false) UUID vendorId,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    
    logRequest("Getting products by brand " + brandId + " for vendor " + vendorId, accountRequest);
    
    PagedResponse<ProductResponse> response = enhancedProductService.getProductsByBrand(
        brandId, vendorId, pageable, accountRequest);
    return ok(response);
  }

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Get product by slug", description = "Get product by SEO-friendly slug")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> getProductBySlug(
      @Parameter(description = "Product slug", required = true) @PathVariable String slug,
      @Parameter(description = "Vendor ID") @RequestParam(required = false) UUID vendorId) {
    
    logPublicRequest("Getting product by slug: " + slug + " for vendor: " + vendorId);
    
    ProductResponse response = enhancedProductService.getProductBySlug(slug, vendorId);
    return ok(response);
  }
} 
