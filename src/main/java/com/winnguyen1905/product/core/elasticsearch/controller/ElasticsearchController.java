package com.winnguyen1905.product.core.elasticsearch.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.core.elasticsearch.service.ProductSearchService;
import com.winnguyen1905.product.core.elasticsearch.service.ProductSyncService;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.core.model.viewmodel.RestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/elasticsearch")
@RequiredArgsConstructor
public class ElasticsearchController {

    private final ProductSearchService productSearchService;
    private final ProductSyncService productSyncService;

    @PostMapping("/search")
    public ResponseEntity<RestResponse<PagedResponse<ProductVariantReviewVm>>> searchProducts(
            @RequestBody SearchProductRequest searchRequest) {
        log.info("Elasticsearch search request: {}", searchRequest);
        
        PagedResponse<ProductVariantReviewVm> response = productSearchService.searchProducts(searchRequest);
        
        return ResponseEntity.ok(
            RestResponse.<PagedResponse<ProductVariantReviewVm>>builder()
                .data(response)
                .message("Products retrieved successfully")
                .build()
        );
    }

    @GetMapping("/suggestions")
    public ResponseEntity<RestResponse<List<String>>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting search suggestions for query: {}", query);
        
        List<String> suggestions = productSearchService.getSearchSuggestions(query);
        
        return ResponseEntity.ok(
            RestResponse.<List<String>>builder()
                .data(suggestions)
                .message("Search suggestions retrieved successfully")
                .build()
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<RestResponse<Page<ProductDocument>>> searchByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching products by category: {}", categoryId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDocument> products = productSearchService.searchByCategory(categoryId, pageable);
        
        return ResponseEntity.ok(
            RestResponse.<Page<ProductDocument>>builder()
                .data(products)
                .message("Products by category retrieved successfully")
                .build()
        );
    }

    @GetMapping("/price-range")
    public ResponseEntity<RestResponse<Page<ProductDocument>>> searchByPriceRange(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching products by price range: {} - {}", minPrice, maxPrice);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDocument> products = productSearchService.searchByPriceRange(minPrice, maxPrice, pageable);
        
        return ResponseEntity.ok(
            RestResponse.<Page<ProductDocument>>builder()
                .data(products)
                .message("Products by price range retrieved successfully")
                .build()
        );
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<RestResponse<List<ProductDocument>>> findSimilarProducts(
            @PathVariable String productId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Finding similar products for: {}", productId);
        
        List<ProductDocument> products = productSearchService.findSimilarProducts(productId, limit);
        
        return ResponseEntity.ok(
            RestResponse.<List<ProductDocument>>builder()
                .data(products)
                .message("Similar products retrieved successfully")
                .build()
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<RestResponse<List<ProductDocument>>> getPopularProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting popular products with limit: {}", limit);
        
        List<ProductDocument> products = productSearchService.getPopularProducts(limit);
        
        return ResponseEntity.ok(
            RestResponse.<List<ProductDocument>>builder()
                .data(products)
                .message("Popular products retrieved successfully")
                .build()
        );
    }

    // Admin endpoints for synchronization
    @PostMapping("/admin/sync/product/{productId}")
    public ResponseEntity<RestResponse<String>> syncProduct(@PathVariable UUID productId) {
        log.info("Syncing product: {}", productId);
        
        productSyncService.syncProduct(productId);
        
        return ResponseEntity.ok(
            RestResponse.<String>builder()
                .data("Product sync initiated")
                .message("Product synchronization started successfully")
                .build()
        );
    }

    @PostMapping("/admin/sync/products")
    public ResponseEntity<RestResponse<String>> syncProducts(@RequestBody List<UUID> productIds) {
        log.info("Syncing {} products", productIds.size());
        
        productSyncService.syncProducts(productIds);
        
        return ResponseEntity.ok(
            RestResponse.<String>builder()
                .data("Products sync initiated")
                .message("Products synchronization started successfully")
                .build()
        );
    }

    @PostMapping("/admin/sync/inventory/{inventoryId}")
    public ResponseEntity<RestResponse<String>> syncInventory(@PathVariable UUID inventoryId) {
        log.info("Syncing inventory: {}", inventoryId);
        
        productSyncService.syncInventory(inventoryId);
        
        return ResponseEntity.ok(
            RestResponse.<String>builder()
                .data("Inventory sync initiated")
                .message("Inventory synchronization started successfully")
                .build()
        );
    }

    @PostMapping("/admin/reindex")
    public ResponseEntity<RestResponse<String>> fullReindex() {
        log.info("Starting full reindex");
        
        productSyncService.fullReindex();
        
        return ResponseEntity.ok(
            RestResponse.<String>builder()
                .data("Full reindex initiated")
                .message("Full reindex started successfully")
                .build()
        );
    }

    @DeleteMapping("/admin/product/{productId}")
    public ResponseEntity<RestResponse<String>> deleteProduct(@PathVariable UUID productId) {
        log.info("Deleting product from Elasticsearch: {}", productId);
        
        productSyncService.deleteProduct(productId);
        
        return ResponseEntity.ok(
            RestResponse.<String>builder()
                .data("Product deletion initiated")
                .message("Product deletion started successfully")
                .build()
        );
    }

    @GetMapping("/admin/health")
    public ResponseEntity<RestResponse<Map<String, Object>>> healthCheck() {
        log.debug("Elasticsearch health check requested");
        
        boolean isHealthy = productSyncService.isHealthy();
        long documentCount = productSyncService.getDocumentCount();
        
        Map<String, Object> healthData = Map.of(
            "healthy", isHealthy,
            "documentCount", documentCount,
            "status", isHealthy ? "UP" : "DOWN"
        );
        
        return ResponseEntity.ok(
            RestResponse.<Map<String, Object>>builder()
                .data(healthData)
                .message("Elasticsearch health check completed")
                .build()
        );
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<RestResponse<Map<String, Object>>> getStats() {
        log.debug("Elasticsearch stats requested");
        
        long documentCount = productSyncService.getDocumentCount();
        
        Map<String, Object> stats = Map.of(
            "totalDocuments", documentCount,
            "indexName", "products"
        );
        
        return ResponseEntity.ok(
            RestResponse.<Map<String, Object>>builder()
                .data(stats)
                .message("Elasticsearch stats retrieved successfully")
                .build()
        );
    }
} 
