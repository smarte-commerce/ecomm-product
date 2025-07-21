package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.winnguyen1905.product.core.model.request.CreateProductImageRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductImageRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.secure.TAccountRequest;

/**
 * Vendor S3 Service Interface
 * 
 * Provides image management services for vendors including:
 * - S3 file upload and management
 * - Product image metadata persistence
 * - Image validation and processing
 * - Bulk operations
 */
public interface VendorS3Service {

  /**
   * Upload multiple files to S3 (simple upload without metadata persistence)
   * 
   * @param files List of files to upload
   * @return List of S3 URLs for uploaded files
   * @throws S3FileException          if upload fails
   * @throws IllegalArgumentException if files are invalid
   */
  List<String> putPackages(List<MultipartFile> files);

  /**
   * Get paginated product images with metadata
   * 
   * @param productId Product ID to get images for
   * @param pageable  Pagination parameters
   * @return Paginated response containing product images
   * @throws ResourceNotFoundException if product not found
   */
  PagedResponse<ProductImageResponse> getProductImages(UUID productId, Pageable pageable);

  /**
   * Get image by ID with full metadata
   * 
   * @param imageId Image ID
   * @return Product image response with metadata
   * @throws ResourceNotFoundException if image not found
   */
  ProductImageResponse getImageById(UUID imageId);

  /**
   * Upload product image with metadata persistence
   * 
   * @param file           Image file to upload
   * @param request        Image creation request with metadata
   * @param accountRequest User account information
   * @return Product image response with metadata and URLs
   * @throws S3FileException           if S3 upload fails
   * @throws ValidationException       if file or request is invalid
   * @throws ResourceNotFoundException if product/variant not found
   */
  ProductImageResponse uploadProductImage(MultipartFile file, CreateProductImageRequest request,
      TAccountRequest accountRequest);

  /**
   * Update image metadata (without replacing the actual file)
   * 
   * @param imageId        Image ID to update
   * @param request        Update request with new metadata
   * @param accountRequest User account information
   * @return Updated product image response
   * @throws ResourceNotFoundException if image not found
   * @throws ValidationException       if request is invalid
   * @throws SecurityException         if user doesn't have permission
   */
  ProductImageResponse updateImageMetadata(UUID imageId, UpdateProductImageRequest request,
      TAccountRequest accountRequest);

  /**
   * Delete image and its metadata
   * 
   * @param imageId        Image ID to delete
   * @param accountRequest User account information
   * @throws ResourceNotFoundException if image not found
   * @throws SecurityException         if user doesn't have permission
   * @throws S3FileException           if S3 deletion fails
   */
  void deleteImage(UUID imageId, TAccountRequest accountRequest);

  /**
   * Bulk upload multiple images for a product/variant
   * 
   * @param files          List of image files
   * @param productId      Product ID
   * @param variantId      Variant ID (optional)
   * @param accountRequest User account information
   * @return List of uploaded image responses
   * @throws S3FileException           if any upload fails
   * @throws ValidationException       if files are invalid
   * @throws ResourceNotFoundException if product/variant not found
   */
  List<ProductImageResponse> bulkUploadProductImages(List<MultipartFile> files, UUID productId, UUID variantId,
      TAccountRequest accountRequest);

  /**
   * Set an image as the primary image for its product
   * This will unset other primary images for the same product
   * 
   * @param imageId        Image ID to set as primary
   * @param accountRequest User account information
   * @return Updated image response
   * @throws ResourceNotFoundException if image not found
   * @throws SecurityException         if user doesn't have permission
   */
  ProductImageResponse setPrimaryImage(UUID imageId, TAccountRequest accountRequest);

  /**
   * Validate file before upload
   * 
   * @param file File to validate
   * @throws ValidationException if file is invalid
   */
  void validateFile(MultipartFile file);

  /**
   * Generate thumbnail URLs for an image
   * 
   * @param originalUrl Original image URL
   * @param imageId     Image ID for unique naming
   * @return List of thumbnail URLs
   */
  List<String> generateThumbnailUrls(String originalUrl, UUID imageId);
}
