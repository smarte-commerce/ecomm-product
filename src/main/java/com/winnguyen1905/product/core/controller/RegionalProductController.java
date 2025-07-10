package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.secure.AccountRequest;
import com.winnguyen1905.product.secure.RegionPartition;
import com.winnguyen1905.product.secure.TAccountRequest;
import com.winnguyen1905.product.service.RegionalQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller demonstrating smart regional query patterns.
 * Shows how AccountRequestArgumentResolver and RegionHibernateFilterConfigurer
 * work together to provide automatic region-based data isolation.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/regional")
public class RegionalProductController {

    private final RegionalQueryService regionalQueryService;

    /**
     * Get products in current user's region (automatic filtering)
     * The region is automatically detected and applied by:
     * 1. AccountRequestArgumentResolver (extracts region from IP/headers/JWT)  
     * 2. RegionHibernateFilterConfigurer (enables Hibernate filters)
     */
    @GetMapping("/products")
    public ResponseEntity<Page<EProduct>> getProductsInCurrentRegion(
            @AccountRequest TAccountRequest account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting products for user in region: {} (page: {}, size: {})", 
                account.region().getCode(), page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        
        // This automatically uses the region filter based on user's detected region
        Page<EProduct> products = regionalQueryService.findProductsInCurrentRegion(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get products from a specific region (override current region)
     */
    @GetMapping("/products/region/{regionCode}")
    public ResponseEntity<Page<EProduct>> getProductsInSpecificRegion(
            @PathVariable String regionCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        RegionPartition targetRegion = RegionPartition.fromCode(regionCode);
        log.info("Getting products specifically from region: {}", targetRegion.getCode());
        
        Pageable pageable = PageRequest.of(page, size);
        
        // This overrides the current region filter
        Page<EProduct> products = regionalQueryService.findProductsInSpecificRegion(targetRegion, pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get vendor products in current region
     */
    @GetMapping("/vendors/{vendorId}/products")
    public ResponseEntity<Page<EProduct>> getVendorProductsInCurrentRegion(
            @AccountRequest TAccountRequest account,
            @PathVariable UUID vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting vendor {} products for user in region: {}", 
                vendorId, account.region().getCode());
        
        Pageable pageable = PageRequest.of(page, size);
        
        // Uses both region and vendor filters
        Page<EProduct> products = regionalQueryService.findVendorProductsInCurrentRegion(vendorId, pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get popular products in current region
     */
    @GetMapping("/products/popular")
    public ResponseEntity<List<EProduct>> getPopularProductsInCurrentRegion(
            @AccountRequest TAccountRequest account,
            @RequestParam(defaultValue = "10") int minPurchases) {
        
        log.info("Getting popular products in region: {} (min purchases: {})", 
                account.region().getCode(), minPurchases);
        
        // Uses current region automatically
        List<EProduct> products = regionalQueryService.findPopularProductsInRegion(
                account.region(), minPurchases);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get categories in current region
     */
    @GetMapping("/categories")
    public ResponseEntity<List<ECategory>> getCategoriesInCurrentRegion(
            @AccountRequest TAccountRequest account) {
        
        log.info("Getting categories for region: {}", account.region().getCode());
        
        // Automatically filtered by region
        List<ECategory> categories = regionalQueryService.findCategoriesInCurrentRegion();
        
        return ResponseEntity.ok(categories);
    }

    /**
     * Get brands in current region
     */
    @GetMapping("/brands")
    public ResponseEntity<List<EBrand>> getBrandsInCurrentRegion(
            @AccountRequest TAccountRequest account) {
        
        log.info("Getting brands for region: {}", account.region().getCode());
        
        // Automatically filtered by region
        List<EBrand> brands = regionalQueryService.findBrandsInCurrentRegion();
        
        return ResponseEntity.ok(brands);
    }

    /**
     * Get low stock items in current region
     */
    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<EProductVariant>> getLowStockItemsInCurrentRegion(
            @AccountRequest TAccountRequest account,
            @RequestParam(defaultValue = "10") int threshold) {
        
        log.info("Getting low stock items in region: {} (threshold: {})", 
                account.region().getCode(), threshold);
        
        List<EProductVariant> variants = regionalQueryService.findLowStockVariantsInCurrentRegion(threshold);
        
        return ResponseEntity.ok(variants);
    }

    /**
     * Get product count in current region
     */
    @GetMapping("/products/count")
    public ResponseEntity<Map<String, Object>> getProductCountInCurrentRegion(
            @AccountRequest TAccountRequest account) {
        
        log.info("Getting product count for region: {}", account.region().getCode());
        
        Long count = regionalQueryService.countProductsInCurrentRegion();
        
        return ResponseEntity.ok(Map.of(
            "region", account.region().getCode(),
            "productCount", count
        ));
    }

    /**
     * Admin endpoint: Get product statistics across all regions
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<RegionalQueryService.RegionalStats> getRegionalStats() {
        
        log.info("Getting product statistics across all regions");
        
        // This bypasses region filtering to get cross-region data
        RegionalQueryService.RegionalStats stats = regionalQueryService.getRegionalProductStats();
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Admin endpoint: Get products from all regions  
     */
    @GetMapping("/admin/products/all")
    public ResponseEntity<Page<EProduct>> getAllRegionProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting products from all regions (admin access)");
        
        Pageable pageable = PageRequest.of(page, size);
        
        // This bypasses region filtering
        Page<EProduct> products = regionalQueryService.findProductsInAllRegions(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Debug endpoint: Check current region and filter status
     */
    @GetMapping("/debug/region-info")
    public ResponseEntity<Map<String, Object>> getRegionDebugInfo(
            @AccountRequest TAccountRequest account) {
        
        return ResponseEntity.ok(Map.of(
            "detectedRegion", account.region().getCode(),
            "regionDisplayName", account.region().getDisplayName(),
            "accountType", account.accountType().toString(),
            "userId", account.id(),
            "isRegionFilterActive", regionalQueryService.isRegionFilterActive(),
            "currentRegionFromContext", regionalQueryService.getCurrentRegion()
                    .map(RegionPartition::getCode).orElse("none")
        ));
    }
} 
