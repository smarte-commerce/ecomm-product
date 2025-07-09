package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.service.EnhancedProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Product Cache Management REST API Controller
 * 
 * Handles cache operations for products including cache eviction and warming
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/cache")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Cache Management", description = "Cache management for products")
public class ProductCacheController extends BaseController {

  private final EnhancedProductService enhancedProductService;

  @PostMapping("/evict/{productId}")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Evict cache success")
  @Operation(summary = "Evict product cache", description = "Evict specific product from cache")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> evictProductCache(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    
    logPublicRequest("Evicting cache for product", productId);
    
    enhancedProductService.evictProductCache(productId);
    return noContent();
  }

  @PostMapping("/warmup")
  // @PreAuthorize("hasRole('ADMIN')")
  @ResponseMessage(message = "Warm up cache success")
  @Operation(summary = "Warm up cache", description = "Warm up product cache by loading frequently accessed products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cache warmed up successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin only")
  })
  public ResponseEntity<Void> warmUpProductCache() {
    
    logPublicRequest("Warming up product cache");
    
    enhancedProductService.warmUpProductCache();
    return noContent();
  }


} 
