package com.winnguyen1905.product.core.service.impl;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.winnguyen1905.product.common.constant.ProductImageType;
import com.winnguyen1905.product.config.S3ConfigurationProperties;
import com.winnguyen1905.product.core.mapper_v2.EnhancedProductMapper;
import com.winnguyen1905.product.core.model.request.CreateProductImageRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductImageRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.core.service.VendorS3Service;
import com.winnguyen1905.product.exception.BusinessLogicException;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.exception.S3FileException;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.ProductImageRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;
import com.winnguyen1905.product.secure.TAccountRequest;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@Transactional
public class VendorS3ServiceImpl implements VendorS3Service {

  private final S3Client s3Client;
  private final S3ConfigurationProperties s3Config;
  private final ProductImageRepository productImageRepository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;

  public VendorS3ServiceImpl(
      S3ConfigurationProperties s3Config,
      ProductImageRepository productImageRepository,
      ProductRepository productRepository,
      ProductVariantRepository productVariantRepository) {
    
    this.s3Config = s3Config;
    this.productImageRepository = productImageRepository;
    this.productRepository = productRepository;
    this.productVariantRepository = productVariantRepository;
    this.s3Client = createS3Client();
  }

  /**
   * Create S3 client with proper configuration
   */
  private S3Client createS3Client() {
    S3ClientBuilder builder = S3Client.builder()
        .region(Region.of(s3Config.getRegion()));

    // Configure credentials
    if (s3Config.isUseIamRole()) {
      builder.credentialsProvider(DefaultCredentialsProvider.create());
    } else if (s3Config.getAccessKey() != null && s3Config.getSecretKey() != null) {
      AwsBasicCredentials credentials = AwsBasicCredentials.create(
          s3Config.getAccessKey(), s3Config.getSecretKey());
      builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
    } else {
      throw new IllegalStateException("S3 credentials not properly configured");
    }

    return builder.build();
  }

  @Override
  public void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessLogicException("File cannot be empty");
    }

    // Validate file size
    if (file.getSize() > parseFileSize(s3Config.getUpload().getMaxFileSize())) {
      throw new BusinessLogicException(
          "File size exceeds maximum allowed: " + s3Config.getUpload().getMaxFileSize());
    }

    // Validate content type
    String contentType = file.getContentType();
    if (!s3Config.isAllowedContentType(contentType)) {
      throw new BusinessLogicException(
          "File type not allowed. Allowed types: " + s3Config.getUpload().getAllowedContentTypes());
    }

    // Validate file extension
    String originalFilename = file.getOriginalFilename();
    if (originalFilename != null) {
      String extension = getFileExtension(originalFilename);
      if (!s3Config.isAllowedExtension(extension)) {
        throw new BusinessLogicException(
            "File extension not allowed. Allowed extensions: " + s3Config.getUpload().getAllowedExtensions());
      }
    }
  }

  @Override
  public List<String> putPackages(List<MultipartFile> files) {
    if (files.size() > s3Config.getUpload().getMaxFilesPerUpload()) {
      throw new BusinessLogicException(
          "Too many files. Maximum allowed: " + s3Config.getUpload().getMaxFilesPerUpload());
    }

    return files.stream()
        .map(file -> {
          validateFile(file);
          return uploadFileToS3(file, generateS3Key(file));
        })
        .collect(Collectors.toList());
  }

  @Override
  @Cacheable(value = "product_images", key = "'product:' + #productId + ':page:' + #pageable.pageNumber")
  public PagedResponse<ProductImageResponse> getProductImages(UUID productId, Pageable pageable) {
    log.info("Getting product images for product ID: {}", productId);
    
    // Verify product exists
    if (!productRepository.existsById(productId)) {
      throw new ResourceNotFoundException("Product not found with ID: " + productId);
    }

    // Use repository method to get images for the product
    Page<EProductImage> imagePage = productImageRepository.findByProductIdAndIsDeletedFalse(productId, pageable);
    
    List<ProductImageResponse> imageResponses = imagePage.getContent().stream()
        .map(this::mapToProductImageResponse)
        .collect(Collectors.toList());

    return new PagedResponse<>(
        imageResponses,
        imagePage.getNumber(),
        imagePage.getSize(),
        imagePage.getTotalElements(),
        imagePage.getTotalPages(),
        imagePage.isLast()
    );
  }

  @Override
  @Cacheable(value = "product_images", key = "#imageId")
  public ProductImageResponse getImageById(UUID imageId) {
    log.info("Getting image with ID: {}", imageId);
    
    EProductImage image = productImageRepository.findByIdAndIsDeletedFalse(imageId)
        .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));
    
    return mapToProductImageResponse(image);
  }

  @Override
  @CacheEvict(value = "product_images", allEntries = true)
  public ProductImageResponse uploadProductImage(MultipartFile file, CreateProductImageRequest request, TAccountRequest accountRequest) {
    log.info("Uploading image for product ID: {} by user ID: {}", request.getProductId(), accountRequest.id());
    
    // Validate input
    validateFile(file);
    validateImageRequest(request, accountRequest);

    // Get product and variant
    EProduct product = getProductById(request.getProductId());
    EProductVariant variant = request.getVariantId() != null ? 
        getProductVariantById(request.getVariantId()) : null;

    // Validate vendor access
    validateVendorAccess(product, accountRequest);

    try {
      // Generate S3 key and upload file
      String s3Key = generateS3Key(file, product.getId(), request.getVariantId());
      String imageUrl = uploadFileToS3(file, s3Key);

      // Generate thumbnail URLs if enabled
      List<String> thumbnailUrls = generateThumbnailUrls(imageUrl, UUID.randomUUID());

      // Create image entity
      EProductImage image = createImageEntity(file, request, product, variant, imageUrl, thumbnailUrls, accountRequest);

      // Handle primary image logic
      if (Boolean.TRUE.equals(request.getIsPrimary())) {
        unsetOtherPrimaryImages(product.getId(), request.getVariantId());
      }

      // Save to database
      image = productImageRepository.save(image);

      log.info("Successfully uploaded image {} for product {}", image.getId(), product.getId());
      return mapToProductImageResponse(image);

    } catch (Exception e) {
      log.error("Error uploading image for product {}: {}", request.getProductId(), e.getMessage());
      throw new S3FileException("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  @Override
  @CacheEvict(value = "product_images", allEntries = true)
  public ProductImageResponse updateImageMetadata(UUID imageId, UpdateProductImageRequest request, TAccountRequest accountRequest) {
    log.info("Updating metadata for image ID: {} by user ID: {}", imageId, accountRequest.id());
    
    EProductImage image = productImageRepository.findByIdAndIsDeletedFalse(imageId)
        .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));

    // Validate vendor access
    validateVendorAccess(image.getProduct(), accountRequest);

    // Update metadata
    updateImageFromRequest(image, request);

    // Handle primary image changes
    if (Boolean.TRUE.equals(request.isPrimary())) {
      unsetOtherPrimaryImages(image.getProduct().getId(), image.getProductVariantId());
    }

    image = productImageRepository.save(image);
    return mapToProductImageResponse(image);
  }

  @Override
  @CacheEvict(value = "product_images", allEntries = true)
  public void deleteImage(UUID imageId, TAccountRequest accountRequest) {
    log.info("Deleting image with ID: {} by user ID: {}", imageId, accountRequest.id());
    
    EProductImage image = productImageRepository.findByIdAndIsDeletedFalse(imageId)
        .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));

    // Validate vendor access
    validateVendorAccess(image.getProduct(), accountRequest);

    try {
      // Delete from S3
      deleteFileFromS3(image.getStorageKey());

      // Soft delete from database
      image.setIsDeleted(true);
      image.setUpdatedBy(accountRequest.id().toString());
      productImageRepository.save(image);

      log.info("Successfully deleted image {} for product {}", imageId, image.getProduct().getId());

    } catch (Exception e) {
      log.error("Error deleting image {}: {}", imageId, e.getMessage());
      throw new S3FileException("Failed to delete image", HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  @Override
  public List<ProductImageResponse> bulkUploadProductImages(List<MultipartFile> files, UUID productId, UUID variantId, TAccountRequest accountRequest) {
    log.info("Bulk uploading {} images for product ID: {} by user ID: {}", files.size(), productId, accountRequest.id());
    
    if (files.size() > s3Config.getUpload().getMaxFilesPerUpload()) {
      throw new BusinessLogicException(
          "Too many files. Maximum allowed: " + s3Config.getUpload().getMaxFilesPerUpload());
    }

    return files.stream().map(file -> {
      CreateProductImageRequest request = CreateProductImageRequest.builder()
          .productId(productId)
          .variantId(variantId)
          .type(ProductImageType.PRODUCT_VARIANT_IMAGE)
          .isPrimary(false)
          .isActive(true)
          .build();
      
      return uploadProductImage(file, request, accountRequest);
    }).collect(Collectors.toList());
  }

  @Override
  @CacheEvict(value = "product_images", allEntries = true)
  public ProductImageResponse setPrimaryImage(UUID imageId, TAccountRequest accountRequest) {
    log.info("Setting image ID: {} as primary by user ID: {}", imageId, accountRequest.id());
    
    EProductImage image = productImageRepository.findByIdAndIsDeletedFalse(imageId)
        .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));

    // Validate vendor access
    validateVendorAccess(image.getProduct(), accountRequest);

    // Unset other primary images
    unsetOtherPrimaryImages(image.getProduct().getId(), image.getProductVariantId());

    // Set this image as primary
    image.setIsPrimary(true);
    image.setDisplayOrder(0);
    image.setUpdatedBy(accountRequest.id().toString());

    image = productImageRepository.save(image);
    return mapToProductImageResponse(image);
  }

  @Override
  public List<String> generateThumbnailUrls(String originalUrl, UUID imageId) {
    if (!s3Config.getImageProcessing().getThumbnails().isEnabled()) {
      return List.of();
    }

    return s3Config.getImageProcessing().getThumbnails().getSizes().stream()
        .map(size -> generateThumbnailUrl(originalUrl, imageId, size.getName(), size.getWidth(), size.getHeight()))
        .collect(Collectors.toList());
  }

  // ================== PRIVATE HELPER METHODS ==================

  private String uploadFileToS3(MultipartFile file, String s3Key) {
    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(s3Config.getPrimaryBucketName())
          .key(s3Key)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

      log.info("File {} uploaded to S3 with key: {}", file.getOriginalFilename(), s3Key);

      return getPublicUrl(s3Key);

    } catch (IOException | S3Exception e) {
      log.error("Failed to upload file {} to S3: {}", file.getOriginalFilename(), e.getMessage());
      throw new S3FileException("Could not upload file to S3", HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  private void deleteFileFromS3(String s3Key) {
    try {
      DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
          .bucket(s3Config.getPrimaryBucketName())
          .key(s3Key)
          .build();

      s3Client.deleteObject(deleteRequest);
      log.info("File deleted from S3 with key: {}", s3Key);

    } catch (S3Exception e) {
      log.error("Failed to delete file from S3 with key {}: {}", s3Key, e.getMessage());
      throw new S3FileException("Could not delete file from S3", HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  private String getPublicUrl(String s3Key) {
    // Check if CDN is enabled
    if (s3Config.isCdnEnabled()) {
      return s3Config.getCdnUrl(s3Key);
    }

    // Fall back to S3 URL
    GetUrlRequest getUrlRequest = GetUrlRequest.builder()
        .bucket(s3Config.getPrimaryBucketName())
        .key(s3Key)
        .build();

    URL url = s3Client.utilities().getUrl(getUrlRequest);
    return url.toString();
  }

  private String generateS3Key(MultipartFile file) {
    return generateS3Key(file, null, null);
  }

  private String generateS3Key(MultipartFile file, UUID productId, UUID variantId) {
    String timestamp = String.valueOf(Instant.now().toEpochMilli());
    String randomId = UUID.randomUUID().toString().substring(0, 8);
    String extension = getFileExtension(file.getOriginalFilename());
    
    StringBuilder keyBuilder = new StringBuilder();
    
    if (productId != null) {
      keyBuilder.append("products/").append(productId).append("/");
      if (variantId != null) {
        keyBuilder.append("variants/").append(variantId).append("/");
      }
    }
    
    keyBuilder.append("images/")
             .append(timestamp)
             .append("_")
             .append(randomId);
    
    if (extension != null && !extension.isEmpty()) {
      keyBuilder.append(".").append(extension);
    }
    
    return keyBuilder.toString();
  }

  private String generateThumbnailUrl(String originalUrl, UUID imageId, String sizeName, int width, int height) {
    // In a real implementation, you'd generate actual thumbnails
    // For now, return a placeholder or the original URL
    return originalUrl + "?size=" + sizeName + "&w=" + width + "&h=" + height;
  }

  private EProductImage createImageEntity(MultipartFile file, CreateProductImageRequest request, 
                                        EProduct product, EProductVariant variant, String imageUrl, 
                                        List<String> thumbnailUrls, TAccountRequest accountRequest) {
    return EProductImage.builder()
        .url(imageUrl)
        .altText(request.getAltText())
        .title(request.getTitle())
        .description(request.getDescription())
        .type(request.getType() != null ? request.getType() : ProductImageType.PRODUCT_VARIANT_IMAGE)
        .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
        .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
        .isActive(request.getIsActive() != null ? request.getIsActive() : true)
        .fileName(file.getOriginalFilename())
        .fileSize(file.getSize())
        .mimeType(file.getContentType())
        .storageProvider("S3")
        .storageBucket(s3Config.getPrimaryBucketName())
        .storageKey(generateS3Key(file, product.getId(), variant != null ? variant.getId() : null))
        .product(product)
        .variant(variant)
        .vendorId(product.getVendorId())
        .isDeleted(false)
        .viewCount(0L)
        .clickCount(0L)
        .createdBy(accountRequest.id().toString())
        .build();
  }

  private void updateImageFromRequest(EProductImage image, UpdateProductImageRequest request) {
    if (request.title() != null) {
      image.setTitle(request.title());
    }
    if (request.altText() != null) {
      image.setAltText(request.altText());
    }
    if (request.description() != null) {
      image.setDescription(request.description());
    }
    if (request.type() != null) {
      image.setType(request.type());
    }
    if (request.displayOrder() != null) {
      image.setDisplayOrder(request.displayOrder());
    }
    if (request.isPrimary() != null) {
      image.setIsPrimary(request.isPrimary());
    }
    if (request.isActive() != null) {
      image.setIsActive(request.isActive());
    }
  }

  private void unsetOtherPrimaryImages(UUID productId, UUID excludeVariantId) {
    List<EProductImage> primaryImages;
    
    if (excludeVariantId != null) {
      // Unset primary for this specific variant
      primaryImages = productImageRepository.findByProductIdAndVariantIdAndIsPrimaryTrueAndIsDeletedFalse(
          productId, excludeVariantId);
    } else {
      // Unset primary for product-level images (where variant is null)
      primaryImages = productImageRepository.findByProductIdAndVariantIsNullAndIsPrimaryTrueAndIsDeletedFalse(productId);
    }

    primaryImages.forEach(image -> {
      image.setIsPrimary(false);
      productImageRepository.save(image);
    });
  }

  private ProductImageResponse mapToProductImageResponse(EProductImage image) {
    ProductImageResponse response = new ProductImageResponse();
    response.setId(image.getId());
    response.setProductId(image.getProduct().getId());
    response.setVariantId(image.getProductVariantId());
    response.setUrl(image.getOptimalUrl());
    response.setFileName(image.getFileName());
    response.setFileSize(image.getFileSize());
    response.setMimeType(image.getMimeType());
    response.setTitle(image.getTitle());
    response.setAltText(image.getAltText());
    response.setDescription(image.getDescription());
    response.setIsPrimary(image.getIsPrimary());
    response.setDisplayOrder(image.getDisplayOrder());
    response.setIsActive(image.getIsActive());
    response.setWidth(image.getWidth());
    response.setHeight(image.getHeight());
    response.setThumbnailUrl(image.getThumbnailUrl());
    response.setSmallUrl(image.getSmallUrl());
    response.setMediumUrl(image.getMediumUrl());
    response.setLargeUrl(image.getLargeUrl());
    return response;
  }

  private EProduct getProductById(UUID productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
  }

  private EProductVariant getProductVariantById(UUID variantId) {
    return productVariantRepository.findById(variantId)
        .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with ID: " + variantId));
  }

  private void validateImageRequest(CreateProductImageRequest request, TAccountRequest accountRequest) {
    if (request.getProductId() == null) {
      throw new BusinessLogicException("Product ID is required");
    }
  }

  private void validateVendorAccess(EProduct product, TAccountRequest accountRequest) {
    if (!product.getVendorId().equals(accountRequest.id()) && !isAdmin(accountRequest)) {
      throw new BusinessLogicException("Access denied: You can only manage your own products");
    }
  }

  private boolean isAdmin(TAccountRequest accountRequest) {
    // Implementation depends on your security setup
    return accountRequest.isAdmin();
  }

  private String getFileExtension(String filename) {
    if (filename == null || filename.isEmpty()) {
      return "";
    }
    int lastDotIndex = filename.lastIndexOf('.');
    return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
  }

  private long parseFileSize(String fileSize) {
    if (fileSize == null || fileSize.isEmpty()) {
      return 0;
    }
    
    String size = fileSize.toLowerCase().trim();
    long multiplier = 1;
    
    if (size.endsWith("kb")) {
      multiplier = 1024;
      size = size.substring(0, size.length() - 2);
    } else if (size.endsWith("mb")) {
      multiplier = 1024 * 1024;
      size = size.substring(0, size.length() - 2);
    } else if (size.endsWith("gb")) {
      multiplier = 1024 * 1024 * 1024;
      size = size.substring(0, size.length() - 2);
    }
    
    try {
      return Long.parseLong(size.trim()) * multiplier;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid file size format: " + fileSize);
    }
  }
}
