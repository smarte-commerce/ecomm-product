package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.ProductImageType;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.CreateProductImageRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductImageRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.core.service.VendorS3Service;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Product Image Management REST API Controller
 * 
 * Provides endpoints for managing product images
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Image Management", description = "APIs for product image operations")
public class ProductImageController extends BaseController {

  private final VendorS3Service s3Service;
  
  @GetMapping("/{productId}/images")
  @Operation(summary = "Get product images", description = "Get all images for a product")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved images"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<PagedResponse<ProductImageResponse>> getProductImages(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @PageableDefault(size = 20) Pageable pageable) {
    logPublicRequest("Getting images for product", productId);
    var result = s3Service.getProductImages(productId, pageable);
    return ok(result);
  }

  @GetMapping("/images/{imageId}")
  @Operation(summary = "Get image by ID", description = "Get image details by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved image"),
    @ApiResponse(responseCode = "404", description = "Image not found")
  })
  public ResponseEntity<ProductImageResponse> getImageById(
      @Parameter(description = "Image ID", required = true) @PathVariable UUID imageId) {
    logPublicRequest("Getting image", imageId);
    var result = s3Service.getImageById(imageId);
    return ok(result);
  }

  @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseMessage(message = "Upload image success")
  @Operation(summary = "Upload product image", description = "Upload a new product image")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
    @ApiResponse(responseCode = "403", description = "Not authorized to upload images"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<ProductImageResponse> uploadImage(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Parameter(description = "Image file", required = true) @RequestParam("file") MultipartFile file,
      @Parameter(description = "Image type") @RequestParam(required = false, defaultValue = "THUMBNAIL") ProductImageType imageType,
      @Parameter(description = "Product variant ID") @RequestParam(required = false) UUID variantId,
      @Parameter(description = "Is primary image") @RequestParam(required = false, defaultValue = "false") Boolean isPrimary,
      @Parameter(description = "Image title") @RequestParam(required = false) String title,
      @Parameter(description = "Image alt text") @RequestParam(required = false) String altText,
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
        
    var result = s3Service.uploadProductImage(file, request, accountRequest);
    return created(result);
  }

  @PutMapping("/images/{imageId}")
  @ResponseMessage(message = "Update image success")
  @Operation(summary = "Update image metadata", description = "Update image metadata without replacing the image")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Image metadata updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid update request"),
    @ApiResponse(responseCode = "403", description = "Not authorized to update images"),
    @ApiResponse(responseCode = "404", description = "Image not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<ProductImageResponse> updateImage(
      @Parameter(description = "Image ID", required = true) @PathVariable UUID imageId,
      @Valid @RequestBody UpdateProductImageRequest request,
      TAccountRequest accountRequest) {
    logRequest("Updating image", imageId, accountRequest);
    var result = s3Service.updateImageMetadata(imageId, request, accountRequest);
    return ok(result);
  }

  @DeleteMapping("/images/{imageId}")
  @ResponseMessage(message = "Delete image success")
  @Operation(summary = "Delete image", description = "Delete a product image")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
    @ApiResponse(responseCode = "403", description = "Not authorized to delete images"),
    @ApiResponse(responseCode = "404", description = "Image not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<Void> deleteImage(
      @Parameter(description = "Image ID", required = true) @PathVariable UUID imageId,
      TAccountRequest accountRequest) {
    logRequest("Deleting image", imageId, accountRequest);
    s3Service.deleteImage(imageId, accountRequest);
    return noContent();
  }
  
  @PostMapping("/images/bulk")
  @ResponseMessage(message = "Bulk upload images success")
  @Operation(summary = "Bulk upload images", description = "Upload multiple images at once")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Images uploaded successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
    @ApiResponse(responseCode = "403", description = "Not authorized to upload images"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<List<ProductImageResponse>> bulkUploadImages(
      @Parameter(description = "Product ID", required = true) @RequestParam UUID productId,
      @Parameter(description = "Image files", required = true) @RequestParam("files") List<MultipartFile> files,
      @Parameter(description = "Product variant ID") @RequestParam(required = false) UUID variantId,
      TAccountRequest accountRequest) {
    logRequest("Bulk uploading " + files.size() + " images for product " + productId, accountRequest);
    var result = s3Service.bulkUploadProductImages(files, productId, variantId, accountRequest);
    return created(result);
  }
  
  @PatchMapping("/images/{imageId}/primary")
  @ResponseMessage(message = "Set primary image success")
  @Operation(summary = "Set primary image", description = "Set an image as the primary image for a product")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Primary image set successfully"),
    @ApiResponse(responseCode = "403", description = "Not authorized to update images"),
    @ApiResponse(responseCode = "404", description = "Image not found")
  })
  // @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
  public ResponseEntity<ProductImageResponse> setPrimaryImage(
      @Parameter(description = "Image ID", required = true) @PathVariable UUID imageId,
      TAccountRequest accountRequest) {
    logRequest("Setting image as primary", imageId, accountRequest);
    var result = s3Service.setPrimaryImage(imageId, accountRequest);
    return ok(result);
  }
}
