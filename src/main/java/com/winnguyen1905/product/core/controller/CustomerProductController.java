package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.ProductAvailabilityRequest;
import com.winnguyen1905.product.core.model.request.ReserveInventoryRequest;
import com.winnguyen1905.product.core.model.request.InventoryConfirmationRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.ProductAvailabilityResponse;
import com.winnguyen1905.product.core.model.response.ReserveInventoryResponse;
import com.winnguyen1905.product.core.model.response.InventoryConfirmationResponse;
import com.winnguyen1905.product.core.model.response.ProductVariantDetailResponse;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Customer Product REST API Controller
 * 
 * Public-facing endpoints for customer product interactions
 * Handles product search, details, availability, and inventory operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customer/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer Products", description = "Public APIs for customer product interactions")
public class CustomerProductController extends BaseController {

  private final CustomerProductService customerProductService;

  // ================== PRODUCT SEARCH & DISCOVERY ==================

  @PostMapping("/search")
  @ResponseMessage(message = "Search products success")
  @Operation(summary = "Search products", description = "Search products with filtering and pagination")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
      @ApiResponse(responseCode = "400", description = "Invalid search parameters")
  })
  public ResponseEntity<PagedResponse<ProductVariantReviewVm>> searchProducts(
      @Valid @RequestBody SearchProductRequest productSearchRequest) {
    
    logPublicRequest("Search products");
    
    PagedResponse<ProductVariantReviewVm> response = customerProductService.searchProducts(productSearchRequest);
    return ok(response);
  }

  // ================== PRODUCT DETAILS ==================

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get product detail success")
  @Operation(summary = "Get product detail", description = "Get complete product details by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductDetailVm> getProductById(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID id) {
    
    logPublicRequest("Get product detail", id);
    
    ProductDetailVm response = customerProductService.getProductDetail(id);
    return ok(response);
  }

  @GetMapping("/{productId}/images")
  @ResponseMessage(message = "Get product images success")
  @Operation(summary = "Get product images", description = "Get all images for a product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved product images"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<PagedResponse<ProductImageVm>> getProductImages(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @PageableDefault(size = 20) Pageable pageable) {
    
    logPublicRequest("Get product images", productId);
    
    PagedResponse<ProductImageVm> response = customerProductService.getProductImages(productId, pageable);
    return ok(response);
  }

  @GetMapping("/{productId}/variants")
  @ResponseMessage(message = "Get product variants success")
  @Operation(summary = "Get product variants", description = "Get all variants for a product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved product variants"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<List<ProductVariantDetailResponse>> getProductVariants(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    
    logPublicRequest("Get product variants", productId);
    
    List<ProductVariantDetailResponse> response = customerProductService.getProductVariantDetails(productId);
    return ok(response);
  }

  // ================== INVENTORY OPERATIONS ==================

  @PostMapping("/availability")
  @ResponseMessage(message = "Check product availability success")
  @Operation(summary = "Check product availability", description = "Check availability for multiple products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully checked availability"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters")
  })
  public ResponseEntity<ProductAvailabilityResponse> checkProductAvailability(
      @Valid @RequestBody ProductAvailabilityRequest request) {
    
    logPublicRequest("Check product availability");
    
    ProductAvailabilityResponse response = customerProductService.checkProductAvailability(request);
    return ok(response);
  }

  @PostMapping("/reserve-inventory")
  @ResponseMessage(message = "Reserve inventory success")
  @Operation(summary = "Reserve inventory", description = "Reserve inventory for products before purchase")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully reserved inventory"),
      @ApiResponse(responseCode = "400", description = "Insufficient inventory or invalid request"),
      @ApiResponse(responseCode = "409", description = "Inventory conflict")
  })
  public ResponseEntity<ReserveInventoryResponse> reserveInventory(
      @Valid @RequestBody ReserveInventoryRequest request) {
    
    logPublicRequest("Reserve inventory");
    
    ReserveInventoryResponse response = customerProductService.reserveInventory(request);
    return ok(response);
  }

  @PostMapping("/inventory-confirmation")
  @ResponseMessage(message = "Inventory confirmation success")
  @Operation(summary = "Confirm inventory", description = "Confirm reserved inventory after purchase")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully confirmed inventory"),
      @ApiResponse(responseCode = "400", description = "Invalid confirmation request"),
      @ApiResponse(responseCode = "404", description = "Reservation not found")
  })
  public ResponseEntity<InventoryConfirmationResponse> inventoryConfirmation(
      @Valid @RequestBody InventoryConfirmationRequest request) {
    
    logPublicRequest("Confirm inventory");
    
    InventoryConfirmationResponse response = customerProductService.inventoryConfirmation(request);
    return ok(response);
  }

  // ================== CART SUPPORT ==================

  @GetMapping("/variant-details/{ids}")
  @ResponseMessage(message = "Get variant details success")
  @Operation(summary = "Get product variant details", description = "Get details for multiple product variants")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved variant details"),
      @ApiResponse(responseCode = "404", description = "Some variants not found")
  })
  public ResponseEntity<ProductVariantByShopVm> getProductCartDetail(
      @Parameter(description = "Variant IDs", required = true) @PathVariable("ids") Set<UUID> ids) {
    
    logPublicRequest("Get variant details");
    
    ProductVariantByShopVm response = customerProductService.getProductVariantDetails(ids);
    return ok(response);
  }
}
