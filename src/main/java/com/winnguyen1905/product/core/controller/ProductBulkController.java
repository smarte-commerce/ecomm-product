package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.CreateProductRequest;
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

import java.util.List;
import java.util.UUID;

/**
 * Product Bulk Operations REST API Controller
 * 
 * Handles bulk operations for products including status updates, deletion, and
 * import
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/bulk")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Bulk Operations", description = "Bulk operations for products")
public class ProductBulkController extends BaseController {

  private final EnhancedProductService enhancedProductService;

  @PatchMapping("/status")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update products status success")
  @Operation(summary = "Bulk update product status", description = "Update status for multiple products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product statuses updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product IDs"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only update own products"),
      @ApiResponse(responseCode = "404", description = "One or more products not found")
  })
  public ResponseEntity<Void> updateProductsStatus(
      @Parameter(description = "List of product IDs", required = true) @RequestBody List<UUID> productIds,
      @Parameter(description = "New status", required = true) @RequestParam ProductStatus status,
      TAccountRequest accountRequest) {

    logRequest("Bulk updating status for " + productIds.size() + " products to " + status, accountRequest);

    enhancedProductService.updateProductsStatus(productIds, status, accountRequest);
    return noContent();
  }

  @PatchMapping("/publish")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Update publish status success")
  @Operation(summary = "Bulk publish/unpublish products", description = "Publish or unpublish multiple products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product publish status updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product IDs"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only update own products"),
      @ApiResponse(responseCode = "404", description = "One or more products not found")
  })
  public ResponseEntity<Void> updateProductsPublishStatus(
      @Parameter(description = "List of product IDs", required = true) @RequestBody List<UUID> productIds,
      @Parameter(description = "Publish status", required = true) @RequestParam Boolean published,
      TAccountRequest accountRequest) {

    logRequest("Bulk " + (published ? "publishing" : "unpublishing") + " " + productIds.size() + " products",
        accountRequest);

    enhancedProductService.updateProductsPublishStatus(productIds, published, accountRequest);
    return noContent();
  }

  @DeleteMapping
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Delete products success")
  @Operation(summary = "Bulk delete products", description = "Soft delete multiple products")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products deleted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product IDs"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only delete own products"),
      @ApiResponse(responseCode = "404", description = "One or more products not found")
  })
  public ResponseEntity<Void> deleteProducts(
      @Parameter(description = "List of product IDs", required = true) @RequestBody List<UUID> productIds,
      TAccountRequest accountRequest) {

    logRequest("Bulk deleting " + productIds.size() + " products", accountRequest);

    enhancedProductService.deleteProducts(productIds, accountRequest);
    return noContent();
  }

  @PostMapping("/import")
  // @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
  @ResponseMessage(message = "Import products success")
  @Operation(summary = "Bulk import products", description = "Import multiple products from a list")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products imported successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request or product data"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "422", description = "Validation failed for one or more products")
  })
  public ResponseEntity<List<ProductResponse>> bulkImportProducts(
      @Parameter(description = "List of product requests", required = true) @Valid @RequestBody List<CreateProductRequest> requests,
      TAccountRequest accountRequest) {

    logRequest("Bulk importing " + requests.size() + " products", accountRequest);

    List<ProductResponse> responses = enhancedProductService.bulkImportProducts(requests, accountRequest);
    return ok(responses);
  }

}
