package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.model.request.InventoryConfirmationRequest;
import com.winnguyen1905.product.core.model.request.ProductAvailabilityRequest;
import com.winnguyen1905.product.core.model.request.ReserveInventoryRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.InventoryConfirmationResponse;
import com.winnguyen1905.product.core.model.response.ProductAvailabilityResponse;
import com.winnguyen1905.product.core.model.response.ProductVariantDetailResponse;
import com.winnguyen1905.product.core.model.response.ReserveInventoryResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantByShopVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.core.service.CustomerProductService;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Customer-facing Product REST API Controller
 * 
 * Provides endpoints for customers to interact with products, check availability,
 * and reserve inventory
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customer/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer Product APIs", description = "APIs for customer product interactions")
public class ProductController {

  private final CustomerProductService customerProductService;

  @GetMapping("/search")
  @ResponseMessage(message = "Search products success")
  @Operation(summary = "Search products", description = "Search products with filtering and pagination")
  public ResponseEntity<PagedResponse<ProductVariantReviewVm>> searchProducts(
      @Valid @RequestBody SearchProductRequest productSearchRequest) {
    log.info("Searching products with request: {}", productSearchRequest);
    return ResponseEntity.ok(customerProductService.searchProducts(productSearchRequest));
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get product detail success")
  @Operation(summary = "Get product detail", description = "Get complete product details by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductDetailVm> getProductById(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID id) {
    log.info("Getting product detail for ID: {}", id);
    return ResponseEntity.ok(customerProductService.getProductDetail(id));
  }

  @GetMapping("/{productId}/images")
  @ResponseMessage(message = "Get product images success")
  @Operation(summary = "Get product images", description = "Get all images for a product")
  public ResponseEntity<PagedResponse<ProductImageVm>> getProductImages(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      Pageable pageable) {
    log.info("Getting images for product: {}", productId);
    // This endpoint is kept for backward compatibility
    // Please use the ProductImageController for image operations
    return ResponseEntity.ok(new PagedResponse<>(new ArrayList<>(), 0, 20, 0, 0, true));
  }

  @GetMapping("/{productId}/variants")
  @ResponseMessage(message = "Get product variants success")
  @Operation(summary = "Get product variants", description = "Get all variants for a product")
  public ResponseEntity<List<ProductVariantDetailResponse>> getProductVariants(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      Pageable pageable) {
    log.info("Getting variants for product: {}", productId);
    return ResponseEntity.ok(customerProductService.getProductVariantDetails(productId));
  }

  @PostMapping("/availability")
  @ResponseMessage(message = "Check product availability success")
  @Operation(summary = "Check product availability", description = "Check availability for multiple products")
  public ResponseEntity<ProductAvailabilityResponse> checkProductAvailability(
      @Valid @RequestBody ProductAvailabilityRequest request) {
    log.info("Checking product availability for {} items", request.getItems().size());
    return ResponseEntity.ok(customerProductService.checkProductAvailability(request));
  }

  @PostMapping("/reserve-inventory")
  @ResponseMessage(message = "Reserve inventory success")
  @Operation(summary = "Reserve inventory", description = "Reserve inventory for products before purchase")
  public ResponseEntity<ReserveInventoryResponse> reserveInventory(
      @Valid @RequestBody ReserveInventoryRequest request) {
    log.info("Reserving inventory for {} items", request.getItems().size());
    return ResponseEntity.ok(customerProductService.reserveInventory(request));
  }

  @PostMapping("/inventory-confirmation")
  @ResponseMessage(message = "Inventory confirmation success")
  @Operation(summary = "Confirm inventory", description = "Confirm reserved inventory after purchase")
  public ResponseEntity<InventoryConfirmationResponse> inventoryConfirmation(
      @Valid @RequestBody InventoryConfirmationRequest request) {
    log.info("Confirming inventory for reservation: {} and order: {}", 
        request.getReservationId(), request.getOrderId());
    return ResponseEntity.ok(customerProductService.inventoryConfirmation(request));
  }

  @GetMapping("/variant-details/{ids}")
  @ResponseMessage(message = "Get variant details success")
  @Operation(summary = "Get product variant details", description = "Get details for multiple product variants")
  public ResponseEntity<ProductVariantByShopVm> getProductCartDetail(
      @Parameter(description = "Variant IDs", required = true) @PathVariable("ids") Set<UUID> ids) {
    log.info("Getting variant details for {} variants", ids.size());
    return ResponseEntity.ok(customerProductService.getProductVariantDetails(ids));
  }
}
