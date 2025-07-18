package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.ProductImageType;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.*;
import com.winnguyen1905.product.core.model.response.*;
import com.winnguyen1905.product.core.model.viewmodel.*;
import com.winnguyen1905.product.core.service.*;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Main Product REST API Controller
 * 
 * Handles all product operations including CRUD, search, customer interactions,
 * image management, and public access
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products", description = "Main product operations")
public class ProductController extends BaseController {

  private final EnhancedProductService productService;
  private final CustomerProductService customerProductService;
  private final VendorS3Service s3Service;

  // ================== PRODUCT CRUD OPERATIONS ==================

  @PostMapping
  @ResponseMessage(message = "Create product success")
  @Operation(summary = "Create product", description = "Create new product")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Product created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  public ResponseEntity<ProductResponse> createProduct(
      @Valid @RequestBody CreateProductRequest request,
      TAccountRequest accountRequest) {
    logRequest("Creating product", accountRequest);
    ProductResponse response = productService.createProduct(request, accountRequest);
    return created(response);
  }

  @GetMapping("/{productId}")
  @Operation(summary = "Get product", description = "Get product details")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> getProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Getting product", productId, accountRequest);
    ProductResponse response = productService.getProduct(productId, accountRequest);
    return ok(response);
  }

  @GetMapping("/public/{productId}")
  @Operation(summary = "Get public product", description = "Get public product details")
  public ResponseEntity<ProductDetailVm> getPublicProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    logPublicRequest("Getting public product", productId);
    ProductDetailVm response = customerProductService.getProductDetail(productId);
    return ok(response);
  }

  @PutMapping("/{productId}")
  @ResponseMessage(message = "Update product success")
  @Operation(summary = "Update product", description = "Update product details")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product updated successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> updateProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Valid @RequestBody UpdateProductRequest request,
      TAccountRequest accountRequest) {
    logRequest("Updating product", productId, accountRequest);
    ProductResponse response = productService.updateProduct(productId, request, accountRequest);
    return ok(response);
  }

  @DeleteMapping("/{productId}")
  @ResponseMessage(message = "Delete product success")
  @Operation(summary = "Delete product", description = "Delete product")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> deleteProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Deleting product", productId, accountRequest);
    productService.deleteProduct(productId, accountRequest);
    return noContent();
  }

  // ================== CUSTOMER OPERATIONS ==================

  @PostMapping("/search")
  @ResponseMessage(message = "Search products success")
  @Operation(summary = "Search products", description = "Search products with filters")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Search completed successfully")
  })
  public ResponseEntity<PagedResponse<ProductVariantReviewVm>> searchProducts(
      @Valid @RequestBody SearchProductRequest request) {
    logPublicRequest("Search products");
    PagedResponse<ProductVariantReviewVm> response = customerProductService.searchProducts(request);
    return ok(response);
  }

  @PostMapping("/availability")
  @ResponseMessage(message = "Check product availability success")
  @Operation(summary = "Check availability", description = "Check product availability")
  public ResponseEntity<ProductAvailabilityResponse> checkAvailability(
      @Valid @RequestBody ProductAvailabilityRequest request) {
    logPublicRequest("Check product availability");
    ProductAvailabilityResponse response = customerProductService.checkProductAvailability(request);
    return ok(response);
  }

  @PostMapping("/reserve-inventory")
  @ResponseMessage(message = "Reserve inventory success")
  @Operation(summary = "Reserve inventory", description = "Reserve inventory for purchase")
  public ResponseEntity<ReserveInventoryResponse> reserveInventory(
      @Valid @RequestBody ReserveInventoryRequest request) {
    logPublicRequest("Reserve inventory");
    ReserveInventoryResponse response = customerProductService.reserveInventory(request);
    return ok(response);
  }

  @PostMapping("/inventory-confirmation")
  @ResponseMessage(message = "Inventory confirmation success")
  @Operation(summary = "Confirm inventory", description = "Confirm reserved inventory")
  public ResponseEntity<InventoryConfirmationResponse> confirmInventory(
      @Valid @RequestBody InventoryConfirmationRequest request) {
    logPublicRequest("Confirm inventory");
    InventoryConfirmationResponse response = customerProductService.inventoryConfirmation(request);
    return ok(response);
  }

  @GetMapping("/{productId}/variants")
  @ResponseMessage(message = "Get product variants success")
  @Operation(summary = "Get variants", description = "Get product variants")
  public ResponseEntity<List<ProductVariantDetailResponse>> getProductVariants(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId) {
    logPublicRequest("Get product variants", productId);
    List<ProductVariantDetailResponse> response = customerProductService.getProductVariantDetails(productId);
    return ok(response);
  }

  @GetMapping("/variant-details/{ids}")
  @ResponseMessage(message = "Get variant details success")
  @Operation(summary = "Get variant details", description = "Get details for multiple variants")
  public ResponseEntity<ProductVariantByShopVm> getVariantDetails(
      @Parameter(description = "Variant IDs", required = true) @PathVariable("ids") Set<UUID> ids) {
    logPublicRequest("Get variant details");
    ProductVariantByShopVm response = customerProductService.getProductVariantDetails(ids);
    return ok(response);
  }

  // ================== IMAGE MANAGEMENT ==================

  @GetMapping("/{productId}/images")
  @Operation(summary = "Get images", description = "Get product images")
  public ResponseEntity<PagedResponse<ProductImageVm>> getProductImages(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @PageableDefault(size = 20) Pageable pageable) {
    logPublicRequest("Getting images for product", productId);
    PagedResponse<ProductImageVm> response = customerProductService.getProductImages(productId, pageable);
    return ok(response);
  }

  @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseMessage(message = "Upload image success")
  @Operation(summary = "Upload image", description = "Upload product image")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid file format")
  })
  public ResponseEntity<ProductImageResponse> uploadImage(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Parameter(description = "Image file", required = true) @RequestParam("file") MultipartFile file,
      @Parameter(description = "Image type") @RequestParam(required = false, defaultValue = "THUMBNAIL") ProductImageType imageType,
      @Parameter(description = "Variant ID") @RequestParam(required = false) UUID variantId,
      @Parameter(description = "Is primary") @RequestParam(required = false, defaultValue = "false") Boolean isPrimary,
      @Parameter(description = "Title") @RequestParam(required = false) String title,
      @Parameter(description = "Alt text") @RequestParam(required = false) String altText,
      TAccountRequest accountRequest) {
    logRequest("Uploading image for product", productId, accountRequest);

    CreateProductImageRequest request = CreateProductImageRequest.builder()
        .productId(productId)
        .variantId(variantId)
        .type(imageType)
        .isPrimary(isPrimary)
        .title(title)
        .altText(altText)
        .build();

    ProductImageResponse response = s3Service.uploadProductImage(file, request, accountRequest);
    return created(response);
  }

  @PutMapping("/images/{imageId}")
  @ResponseMessage(message = "Update image success")
  @Operation(summary = "Update image", description = "Update image metadata")
  public ResponseEntity<ProductImageResponse> updateImage(
      @Parameter(description = "Image ID", required = true) @PathVariable UUID imageId,
      @Valid @RequestBody UpdateProductImageRequest request,
      TAccountRequest accountRequest) {
    logRequest("Updating image", imageId, accountRequest);
    ProductImageResponse response = s3Service.updateImageMetadata(imageId, request, accountRequest);
    return ok(response);
  }

  @DeleteMapping("/images/{imageId}")
  @ResponseMessage(message = "Delete image success")
  @Operation(summary = "Delete image", description = "Delete product image")
  public ResponseEntity<Void> deleteImage(
      @Parameter(description = "Image ID", required = true) @PathVariable UUID imageId,
      TAccountRequest accountRequest) {
    logRequest("Deleting image", imageId, accountRequest);
    s3Service.deleteImage(imageId, accountRequest);
    return noContent();
  }

  @PostMapping("/images/bulk")
  @ResponseMessage(message = "Bulk upload images success")
  @Operation(summary = "Bulk upload images", description = "Upload multiple images")
  public ResponseEntity<List<ProductImageResponse>> bulkUploadImages(
      @Parameter(description = "Product ID", required = true) @RequestParam UUID productId,
      @Parameter(description = "Image files", required = true) @RequestParam("files") List<MultipartFile> files,
      @Parameter(description = "Variant ID") @RequestParam(required = false) UUID variantId,
      TAccountRequest accountRequest) {
    logRequest("Bulk uploading " + files.size() + " images for product " + productId, accountRequest);
    List<ProductImageResponse> response = s3Service.bulkUploadProductImages(files, productId, variantId,
        accountRequest);
    return created(response);
  }

  // ================== RESTORE OPERATION ==================

  @PatchMapping("/{productId}/restore")
  @ResponseMessage(message = "Restore product success")
  @Operation(summary = "Restore product", description = "Restore deleted product")
  public ResponseEntity<ProductResponse> restoreProduct(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Restoring product", productId, accountRequest);
    ProductResponse response = productService.restoreProduct(productId, accountRequest);
    return ok(response);
  }
}
