package com.winnguyen1905.product.core.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.elasticsearch.service.ProductSearchService;
import com.winnguyen1905.product.core.elasticsearch.service.ProductSyncService;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
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
 * Elasticsearch Management REST API Controller
 * 
 * Administrative endpoints for managing Elasticsearch operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/elasticsearch")
@RequiredArgsConstructor
@Validated
@Tag(name = "Elasticsearch Management", description = "Administrative APIs for Elasticsearch operations")
public class ElasticsearchController extends BaseController {

  private final ProductSearchService productSearchService;
  private final ProductSyncService productSyncService;

  // ================== SEARCH OPERATIONS ==================

  @PostMapping("/search")
  @ResponseMessage(message = "Search products success")
  @Operation(summary = "Search products", description = "Advanced product search with Elasticsearch")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products found successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid search request")
  })
  public ResponseEntity<PagedResponse<ProductVariantReviewVm>> searchProducts(
      @Valid @RequestBody SearchProductRequest searchRequest) {
    logPublicRequest("Elasticsearch search with term: " + searchRequest.getKeyword());
    var result = productSearchService.searchProducts(searchRequest);
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
    var suggestions = productSearchService.getSearchSuggestions(term);
    return ok(suggestions);
  }

  // ================== ADMIN SYNC OPERATIONS ==================

  @PostMapping("/sync/product/{productId}")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Sync product success")
  @Operation(summary = "Sync single product", description = "Sync a single product to Elasticsearch")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product synced successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> syncProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Syncing product to Elasticsearch", productId, accountRequest);
    productSyncService.syncProduct(productId);
    return noContent();
  }

  @PostMapping("/sync/products")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Sync products success")
  @Operation(summary = "Sync multiple products", description = "Sync multiple products to Elasticsearch")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products synced successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only")
  })
  public ResponseEntity<Void> syncProducts(
      @Parameter(description = "Product IDs", required = true) @RequestBody List<UUID> productIds,
      TAccountRequest accountRequest) {
    logRequest("Syncing multiple products to Elasticsearch", accountRequest, String.valueOf(productIds.size()));
    productSyncService.syncProducts(productIds);
    return noContent();
  }

  @PostMapping("/sync/vendor/{vendorId}")
  // @PreAuthorize("hasRole('ADMIN') or (hasRole('VENDOR') and #vendorId ==
  // #accountRequest.id())")
  @ResponseMessage(message = "Sync vendor products success")
  @Operation(summary = "Sync vendor products", description = "Sync all products for a vendor to Elasticsearch")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Vendor products synced successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  public ResponseEntity<Void> syncVendorProducts(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      TAccountRequest accountRequest) {
    logRequest("Syncing products for vendor to Elasticsearch", vendorId, accountRequest);
    // TODO: Implement vendor-specific sync method
    log.warn("syncVendorProducts not yet implemented for vendor: {}", vendorId);
    return noContent();
  }

  @PostMapping("/reindex")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Full reindex started")
  @Operation(summary = "Full reindex", description = "Perform full reindex of all products to Elasticsearch")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Reindex started successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only")
  })
  public ResponseEntity<Void> fullReindex(TAccountRequest accountRequest) {
    logRequest("Starting full reindex of products", accountRequest, "ALL");
    productSyncService.fullReindex();
    return noContent();
  }

  @PostMapping("/sync/inventory/{productId}")
  // @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
  @ResponseMessage(message = "Sync inventory success")
  @Operation(summary = "Sync product inventory", description = "Sync product inventory data to Elasticsearch")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Inventory synced successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> syncProductInventory(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Syncing inventory for product", productId, accountRequest);
    // Use existing syncProduct method as it includes inventory data
    productSyncService.syncProduct(productId);
    return noContent();
  }

  @DeleteMapping("/product/{productId}")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Delete from index success")
  @Operation(summary = "Delete product from index", description = "Remove product from Elasticsearch index")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product deleted from index"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
      @ApiResponse(responseCode = "404", description = "Product not found in index")
  })
  public ResponseEntity<Void> deleteProductFromIndex(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Deleting product from Elasticsearch index", productId, accountRequest);
    productSyncService.deleteProduct(productId);
    return noContent();
  }

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
  // @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get index statistics", description = "Get detailed Elasticsearch index statistics")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Index statistics retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only")
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
}
