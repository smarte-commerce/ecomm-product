package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.CreateProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
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

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Product CRUD Operations REST API Controller
 * 
 * Handles basic Create, Read, Update, Delete operations for products
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product CRUD", description = "Basic product CRUD operations")
public class ProductCrudController extends BaseController {

  private final EnhancedProductService enhancedProductService;

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
    
    logRequest("Creating product", accountRequest);
    
    try {
      ProductResponse response = enhancedProductService.createProduct(request, accountRequest);
      return created(response);
    } catch (Exception e) {
      log.error("Error creating product for vendor {}: {}", accountRequest.id(), e.getMessage());
      throw e;
    }
  }

  @GetMapping("/{productId}")
  @Operation(summary = "Get product details", description = "Get detailed product information")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> getProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    
    logRequest("Getting product", productId, accountRequest);
    
    ProductResponse response = enhancedProductService.getProduct(productId, accountRequest);
    return ok(response);
  }

  @GetMapping("/public/{productId}")
  @Operation(summary = "Get public product", description = "Get public product information without authentication")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> getPublicProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    
    logPublicRequest("Getting public product", productId);
    
    ProductResponse response = enhancedProductService.getPublicProduct(productId);
    return ok(response);
  }

  @PutMapping("/{productId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update product success")
  @Operation(summary = "Update product", description = "Update product information with optimistic locking")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "409", description = "Optimistic locking conflict")
  })
  public ResponseEntity<ProductResponse> updateProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Valid @RequestBody UpdateProductRequest request,
      TAccountRequest accountRequest) {
    
    logRequest("Updating product", productId, accountRequest);
    
    ProductResponse response = enhancedProductService.updateProduct(productId, request, accountRequest);
    return ok(response);
  }

  @DeleteMapping("/{productId}")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Delete product success")
  @Operation(summary = "Delete product", description = "Soft delete product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  public ResponseEntity<Void> deleteProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    
    logRequest("Deleting product", productId, accountRequest);
    
    enhancedProductService.deleteProduct(productId, accountRequest);
    return noContent();
  }

  @PatchMapping("/{productId}/restore")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Restore product success")
  @Operation(summary = "Restore product", description = "Restore deleted product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product restored successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  public ResponseEntity<ProductResponse> restoreProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    
    logRequest("Restoring product", productId, accountRequest);
    
    ProductResponse response = enhancedProductService.restoreProduct(productId, accountRequest);
    return ok(response);
  }
} 
