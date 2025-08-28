package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.elasticsearch.service.ProductSearchService;
import com.winnguyen1905.product.core.elasticsearch.service.ProductSyncService;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.core.service.EnhancedProductService;
import com.winnguyen1905.product.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product Search REST API Controller
 * 
 * Handles all product search operations including Elasticsearch,
 * sync operations, suggestions, and analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@Validated
@Tag(name = "Product Search", description = "All product search and Elasticsearch operations")
public class ProductSearchController extends BaseController {

  private final EnhancedProductService enhancedProductService;
  
  // Optional Elasticsearch services - will be null in local profile
  @Autowired(required = false)
  private ProductSearchService productSearchService;
  
  @Autowired(required = false)
  private ProductSyncService productSyncService;
  
  public ProductSearchController(EnhancedProductService enhancedProductService) {
    this.enhancedProductService = enhancedProductService;
  }

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

  // ================== ELASTICSEARCH OPERATIONS ==================

  @PostMapping("/elasticsearch")
  @ResponseMessage(message = "Elasticsearch search success")
  @Operation(summary = "Elasticsearch search", description = "Advanced product search with Elasticsearch")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products found successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid search request")
  })
  public ResponseEntity<PagedResponse<ProductVariantReviewVm>> searchWithElasticsearch(
      @Valid @RequestBody SearchProductRequest searchRequest) {
    logPublicRequest("Elasticsearch search with term: " + searchRequest.getKeyword());
    PagedResponse<ProductVariantReviewVm> result = productSearchService.searchProducts(searchRequest);
    return ok(result);
  }

  @GetMapping("/suggestions")
  @Operation(summary = "Get search suggestions", description = "Get autocomplete suggestions for search")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully")
  })
  public ResponseEntity<List<String>> getSearchSuggestions(
      @Parameter(description = "Search term", required = true) @RequestParam String term) {
    logPublicRequest("Getting search suggestions for: " + term);
    List<String> suggestions = productSearchService.getSearchSuggestions(term);
    return ok(suggestions);
  }

  // ================== SYNC OPERATIONS (Admin) ==================

  // @PostMapping("/sync/product/{productId}")
  // @ResponseMessage(message = "Sync product success")
  // @Operation(summary = "Sync single product", description = "Sync a single product to Elasticsearch")
  // @ApiResponses({
  //     @ApiResponse(responseCode = "200", description = "Product synced successfully"),
  //     @ApiResponse(responseCode = "404", description = "Product not found"),
  //     @ApiResponse(responseCode = "503", description = "Elasticsearch service not available")
  // })
  // public ResponseEntity<Void> syncProduct(
  //     @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
  //     TAccountRequest accountRequest) {
  //   logRequest("Syncing product to Elasticsearch", productId, accountRequest);
    
  //   if (productSyncService != null) {
  //     productSyncService.syncProduct(productId);
  //   } else {
  //     log.warn("Elasticsearch sync service not available - skipping sync for product: {}", productId);
  //   }
    
  //   return noContent();
  // }

  // @PostMapping("/sync/products")
  // @ResponseMessage(message = "Sync products success")
  // @Operation(summary = "Sync multiple products", description = "Sync multiple products to Elasticsearch")
  // @ApiResponses({
  //     @ApiResponse(responseCode = "200", description = "Products synced successfully"),
  //     @ApiResponse(responseCode = "503", description = "Elasticsearch service not available")
  // })
  // public ResponseEntity<Void> syncProducts(
  //     @Parameter(description = "Product IDs", required = true) @RequestBody List<UUID> productIds,
  //     TAccountRequest accountRequest) {
  //   logRequest("Syncing multiple products to Elasticsearch", accountRequest, String.valueOf(productIds.size()));
    
  //   if (productSyncService != null) {
  //     productSyncService.syncProducts(productIds);
  //   } else {
  //     log.warn("Elasticsearch sync service not available - skipping sync for {} products", productIds.size());
  //   }
    
  //   return noContent();
  // }

  // @PostMapping("/reindex")
  // @ResponseMessage(message = "Full reindex started")
  // @Operation(summary = "Full reindex", description = "Perform full reindex of all products to Elasticsearch")
  // @ApiResponses({
  //     @ApiResponse(responseCode = "200", description = "Reindex started successfully")
  // })
  // public ResponseEntity<Void> fullReindex(TAccountRequest accountRequest) {
  //   logRequest("Starting full reindex of products", accountRequest, "ALL");
  //   productSyncService.fullReindex();
  //   return noContent();
  // }

  // @DeleteMapping("/product/{productId}")
  // @ResponseMessage(message = "Delete from index success")
  // @Operation(summary = "Delete product from index", description = "Remove product from Elasticsearch index")
  // @ApiResponses({
  //     @ApiResponse(responseCode = "200", description = "Product deleted from index"),
  //     @ApiResponse(responseCode = "404", description = "Product not found in index")
  // })
  // public ResponseEntity<Void> deleteProductFromIndex(
  //     @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
  //     TAccountRequest accountRequest) {
  //   logRequest("Deleting product from Elasticsearch index", productId, accountRequest);
  //   productSyncService.deleteProduct(productId);
  //   return noContent();
  // }

  // ================== HEALTH & STATISTICS ==================

  @GetMapping("/health")
  @Operation(summary = "Check index health", description = "Check Elasticsearch index health and statistics")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Index health retrieved successfully")
  })
  public ResponseEntity<String> checkIndexHealth() {
    logPublicRequest("Checking Elasticsearch index health");
    boolean isHealthy = productSyncService.isHealthy();
    String message = isHealthy ? "Elasticsearch index is healthy" : "Elasticsearch index has issues";
    return ok(message);
  }

  @GetMapping("/stats")
  @Operation(summary = "Get index statistics", description = "Get detailed Elasticsearch index statistics")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Index statistics retrieved successfully")
  })
  public ResponseEntity<Object> getIndexStats(TAccountRequest accountRequest) {
    logRequest("Getting Elasticsearch index statistics", accountRequest, "STATS");
    long documentCount = productSyncService.getDocumentCount();
    Map<String, Object> stats = Map.of(
        "documentCount", documentCount,
        "indexName", "products",
        "isHealthy", productSyncService.isHealthy(),
        "timestamp", java.time.Instant.now());
    return ok(stats);
  }

  @PostMapping("/admin/recreate-index")
  @Operation(summary = "Recreate Elasticsearch index", description = "Recreate the products index with proper settings and mappings")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Index recreated successfully"),
      @ApiResponse(responseCode = "500", description = "Failed to recreate index")
  })
  public ResponseEntity<Object> recreateIndex(TAccountRequest accountRequest) {
    logRequest("Recreating Elasticsearch index", accountRequest, "RECREATE_INDEX");
    productSyncService.recreateIndex();
    return ok(Map.of("message", "Elasticsearch index recreated successfully", "timestamp", java.time.Instant.now()));
  }
} 
