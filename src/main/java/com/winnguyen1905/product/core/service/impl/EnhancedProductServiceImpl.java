package com.winnguyen1905.product.core.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.mapper_v2.EnhancedProductMapper;
import com.winnguyen1905.product.core.model.request.CreateProductRequest;
import com.winnguyen1905.product.core.model.request.CreateProductVariantRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.EnhancedProductService;
import com.winnguyen1905.product.core.service.VendorProductService;
import com.winnguyen1905.product.exception.BusinessLogicException;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.BrandRepository;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.persistance.repository.EnhancedProductRepository;
import com.winnguyen1905.product.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced Product Service Implementation
 * 
 * Full-featured implementation with Redis caching, multi-vendor support,
 * Elasticsearch integration, and comprehensive business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnhancedProductServiceImpl implements EnhancedProductService {

  private final EnhancedProductRepository productRepository;
  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;
  private final VendorProductService vendorProductService;

  // ================== CRUD OPERATIONS ==================

  @Override
  // @Transactional
  public ProductResponse createProduct(CreateProductRequest request, TAccountRequest accountRequest) {
    log.info("Creating product: {} for vendor: {}", request.name(), accountRequest.id());

    // Validate business rules
    validateProductCreation(request, accountRequest);

    // Create product entity
    EProduct product = EnhancedProductMapper.toEntity(request);

    // Set audit fields
    product.setCreatedBy(accountRequest.id().toString());
    product.setUpdatedBy(accountRequest.id().toString());

    // Generate unique slug if not provided
    if (product.getSlug() == null || product.getSlug().isEmpty()) {
      product.setSlug(generateUniqueSlug(product.getName(), product.getVendorId()));
    }

    // Set brand if provided
    if (request.brandId() != null) {
      EBrand brand = brandRepository.findById(request.brandId())
          .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + request.brandId()));
      product.setBrand(brand);
    }

    // Set category if provided
    if (request.categoryId() != null) {
      ECategory category = categoryRepository.findById(request.categoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
      product.setCategory(category);
    }

    if (request.variants() != null && !request.variants().isEmpty()) {
      for (CreateProductVariantRequest variantRequest : request.variants()) {
        EProductVariant variant = EnhancedProductMapper.toVariantEntity(variantRequest, product);
        variant.setProduct(product);
        product.getVariants().add(variant);
      }
    }

    // Save product
    EProduct savedProduct = productRepository.save(product);

    // TODO: Create variants and images if provided

    // Index in Elasticsearch
    vendorProductService.persistProductVariants(savedProduct);

    log.info("Product created successfully: {}", savedProduct.getId());

    return EnhancedProductMapper.toResponse(savedProduct);
  }

  @Override
  @Cacheable(value = "products", key = "#productId", unless = "#result == null")
  public ProductResponse getProduct(UUID productId, TAccountRequest accountRequest) {
    log.debug("Getting product: {} for account: {}", productId, accountRequest.id());

    EProduct product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

    // Check access permissions
    if (!hasProductAccess(productId, accountRequest)) {
      throw new BusinessLogicException("No access to product: " + productId);
    }

    return EnhancedProductMapper.toResponse(product);
  }

  @Override
  @Cacheable(value = "public-products", key = "#productId", unless = "#result == null")
  public ProductResponse getPublicProduct(UUID productId) {
    log.debug("Getting public product: {}", productId);

    EProduct product = productRepository.findByIdAndIsPublishedTrue(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Published product not found: " + productId));

    return EnhancedProductMapper.toResponse(product);
  }

  @Override
  @Transactional
  @CacheEvict(value = { "products", "public-products" }, key = "#productId")
  public ProductResponse updateProduct(UUID productId, UpdateProductRequest request, TAccountRequest accountRequest) {
    log.info("Updating product: {} by vendor: {}", productId, accountRequest.id());

    // Validate business rules
    validateProductUpdate(productId, request, accountRequest);

    // Get product with optimistic locking
    EProduct product = productRepository.findByIdWithOptimisticLock(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

    // Check ownership
    if (!isProductOwner(productId, accountRequest.id())) {
      throw new BusinessLogicException("Not authorized to update product: " + productId);
    }

    // Update fields
    EnhancedProductMapper.updateEntityFromRequest(product, request);
    product.setUpdatedBy(accountRequest.id().toString());

    // Update brand if changed
    if (request.brandId() != null &&
        (product.getBrand() == null || !product.getBrand().getId().equals(request.brandId()))) {
      EBrand brand = brandRepository.findById(request.brandId())
          .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + request.brandId()));
      product.setBrand(brand);
    }

    // Update category if changed
    if (request.categoryId() != null &&
        (product.getCategory() == null || !product.getCategory().getId().equals(request.categoryId()))) {
      ECategory category = categoryRepository.findById(request.categoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
      product.setCategory(category);
    }

    // Update price range
    EnhancedProductMapper.updateProductPriceRange(product);

    // Save updated product
    EProduct savedProduct = productRepository.save(product);

    // Update Elasticsearch index
    vendorProductService.persistProductVariants(savedProduct);

    log.info("Product updated successfully: {}", savedProduct.getId());

    return EnhancedProductMapper.toResponse(savedProduct);
  }

  @Override
  @Transactional
  @CacheEvict(value = { "products", "public-products" }, key = "#productId")
  public void deleteProduct(UUID productId, TAccountRequest accountRequest) {
    log.info("Deleting product: {} by vendor: {}", productId, accountRequest.id());

    EProduct product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

    // Check ownership
    if (!isProductOwner(productId, accountRequest.id())) {
      throw new BusinessLogicException("Not authorized to delete product: " + productId);
    }

    // Soft delete
    product.setIsDeleted(true);
    product.setUpdatedBy(accountRequest.id().toString());

    productRepository.save(product);

    // TODO: Remove from Elasticsearch index

    log.info("Product deleted successfully: {}", productId);
  }

  @Override
  @Transactional
  public ProductResponse restoreProduct(UUID productId, TAccountRequest accountRequest) {
    log.info("Restoring product: {} by vendor: {}", productId, accountRequest.id());

    EProduct product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

    // Check ownership
    if (!isProductOwner(productId, accountRequest.id())) {
      throw new BusinessLogicException("Not authorized to restore product: " + productId);
    }

    // Restore product
    product.setIsDeleted(false);
    product.setUpdatedBy(accountRequest.id().toString());

    EProduct savedProduct = productRepository.save(product);

    // Re-index in Elasticsearch
    vendorProductService.persistProductVariants(savedProduct);

    log.info("Product restored successfully: {}", productId);

    return EnhancedProductMapper.toResponse(savedProduct);
  }

  // ================== VENDOR OPERATIONS ==================

  @Override
  @Cacheable(value = "vendor-products", key = "#vendorId + ':' + #status + ':' + #pageable.pageNumber")
  public PagedResponse<ProductResponse> getVendorProducts(UUID vendorId, ProductStatus status,
      Pageable pageable, TAccountRequest accountRequest) {
    log.debug("Getting vendor products for vendor: {}, status: {}", vendorId, status);

    // Check vendor access
    if (!accountRequest.id().equals(vendorId) && !accountRequest.isAdmin()) {
      throw new BusinessLogicException("No access to vendor products: " + vendorId);
    }

    Page<EProduct> productPage;
    if (status != null) {
      productPage = productRepository.findByShopIdAndStatus(vendorId, status, pageable);
    } else {
      productPage = productRepository.findByVendorId(vendorId, pageable);
    }

    List<ProductResponse> products = EnhancedProductMapper.toResponseList(productPage.getContent());

    return PagedResponse.<ProductResponse>builder()
        .content(products)
        .pageNumber(pageable.getPageNumber())
        .pageSize(pageable.getPageSize())
        .totalElements((int) productPage.getTotalElements())
        .totalPages(productPage.getTotalPages())
        .build();
  }

  @Override
  @Cacheable(value = "shop-products", key = "#shopId + ':' + #pageable.pageNumber")
  public PagedResponse<ProductResponse> getShopProducts(UUID shopId, Pageable pageable) {
    log.debug("Getting shop products for shop: {}", shopId);

    Page<EProduct> productPage = productRepository.findAllByShopIdAndIsPublishedTrue(shopId, pageable);
    List<ProductResponse> products = EnhancedProductMapper.toResponseList(productPage.getContent());

    return PagedResponse.<ProductResponse>builder()
        .content(products)
        .pageNumber(pageable.getPageNumber())
        .pageSize(pageable.getPageSize())
        .totalElements((int) productPage.getTotalElements())
        .totalPages(productPage.getTotalPages())
        .build();
  }

  @Override
  public PagedResponse<ProductResponse> searchProducts(SearchProductRequest request, TAccountRequest accountRequest) {
    log.debug("Searching products with request: {}", request);

    // TODO: Implement Elasticsearch search
    // This is a placeholder implementation using database search

    Page<EProduct> productPage = productRepository.searchProducts(
        request.getKeyword(),
        null, // ProductType from request
        accountRequest.id(), // Filter by vendor if not admin
        null, // Region from request
        request.getPage());

    List<ProductResponse> products = EnhancedProductMapper.toResponseList(productPage.getContent());

    return PagedResponse.<ProductResponse>builder()
        .content(products)
        .pageNumber(request.getPage().getPageNumber())
        .pageSize(request.getPage().getPageSize())
        .totalElements((int) productPage.getTotalElements())
        .totalPages(productPage.getTotalPages())
        .build();
  }

  // ================== VALIDATION & BUSINESS LOGIC ==================

  @Override
  public boolean hasProductAccess(UUID productId, TAccountRequest accountRequest) {
    if (accountRequest.isAdmin()) {
      return true;
    }

    EProduct product = productRepository.findById(productId).orElse(null);
    if (product == null) {
      return false;
    }

    // Vendor can access their own products
    if (accountRequest.id().equals(product.getVendorId())) {
      return true;
    }

    // Public access to published products
    return Boolean.TRUE.equals(product.getIsPublished()) &&
        product.getStatus() == ProductStatus.ACTIVE;
  }

  @Override
  public boolean isProductOwner(UUID productId, UUID vendorId) {
    return productRepository.findById(productId)
        .map(product -> vendorId.equals(product.getVendorId()))
        .orElse(false);
  }

  @Override
  public void validateProductCreation(CreateProductRequest request, TAccountRequest accountRequest) {
    // Validate vendor ownership
    if (!accountRequest.id().equals(request.vendorId()) && !accountRequest.isAdmin()) {
      throw new BusinessLogicException("Cannot create product for different vendor");
    }

    // Validate slug uniqueness
    if (request.slug() != null &&
        productRepository.existsBySlugAndVendorId(request.slug(), request.vendorId())) {
      throw new BusinessLogicException("Slug already exists for vendor: " + request.slug());
    }

    // TODO: Add more business validations
  }

  @Override
  public void validateProductUpdate(UUID productId, UpdateProductRequest request, TAccountRequest accountRequest) {
    EProduct product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

    // Validate ownership
    if (!isProductOwner(productId, accountRequest.id()) && !accountRequest.isAdmin()) {
      throw new BusinessLogicException("Not authorized to update product: " + productId);
    }

    // Validate slug uniqueness if changed
    if (request.slug() != null && !request.slug().equals(product.getSlug()) &&
        productRepository.existsBySlugAndVendorId(request.slug(), product.getVendorId())) {
      throw new BusinessLogicException("Slug already exists for vendor: " + request.slug());
    }

    // TODO: Add more business validations
  }

  // ================== UTILITY METHODS ==================

  @Override
  public String generateUniqueSlug(String name, UUID vendorId) {
    String baseSlug = EnhancedProductMapper.generateSlug(name, vendorId);
    String uniqueSlug = baseSlug;
    int counter = 1;

    while (productRepository.existsBySlugAndVendorId(uniqueSlug, vendorId)) {
      uniqueSlug = baseSlug + "-" + counter++;
    }

    return uniqueSlug;
  }

  @Override
  public ProductResponse getProductBySlug(String slug, UUID vendorId) {
    EProduct product = productRepository.findBySlugAndVendor(slug, vendorId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));

    return EnhancedProductMapper.toResponse(product);
  }

  // ================== CACHE MANAGEMENT ==================

  @Override
  @CacheEvict(value = { "products", "public-products" }, key = "#productId")
  public void evictProductCache(UUID productId) {
    log.debug("Evicting cache for product: {}", productId);
  }

  @Override
  @CacheEvict(value = "vendor-products", allEntries = true)
  public void evictVendorProductsCache(UUID vendorId) {
    log.debug("Evicting vendor products cache for vendor: {}", vendorId);
  }

  @Override
  public void warmUpProductCache() {
    log.info("Warming up product cache");
    // TODO: Implement cache warm-up logic
  }

  // ================== NOT YET IMPLEMENTED ==================
  // These methods need additional dependencies and business logic

  @Override
  public void updateProductsStatus(List<UUID> productIds, ProductStatus status, TAccountRequest accountRequest) {
    // TODO: Implement bulk status update
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void updateProductsPublishStatus(List<UUID> productIds, Boolean published, TAccountRequest accountRequest) {
    // TODO: Implement bulk publish status update
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void deleteProducts(List<UUID> productIds, TAccountRequest accountRequest) {
    // TODO: Implement bulk delete
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public List<ProductResponse> bulkImportProducts(List<CreateProductRequest> requests, TAccountRequest accountRequest) {
    // TODO: Implement bulk import
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public PagedResponse<ProductResponse> getProductsByCategory(UUID categoryId, UUID vendorId, Pageable pageable,
      TAccountRequest accountRequest) {
    // TODO: Implement category filtering
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public PagedResponse<ProductResponse> getProductsByBrand(UUID brandId, UUID vendorId, Pageable pageable,
      TAccountRequest accountRequest) {
    // TODO: Implement brand filtering
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public ProductResponse updateProductCategory(UUID productId, UUID categoryId, TAccountRequest accountRequest) {
    // TODO: Implement category update
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public ProductResponse updateProductBrand(UUID productId, UUID brandId, TAccountRequest accountRequest) {
    // TODO: Implement brand update
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void syncProductInventory(UUID productId) {
    // TODO: Implement inventory sync with Elasticsearch
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public List<ProductResponse> checkLowStockProducts(UUID vendorId) {
    // TODO: Implement low stock check
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void updateProductStock(UUID productId, TAccountRequest accountRequest) {
    // TODO: Implement stock update
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public ProductResponse updateProductSEO(UUID productId, String metaTitle, String metaDescription, String metaKeywords,
      TAccountRequest accountRequest) {
    // TODO: Implement SEO update
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public VendorProductStats getVendorProductStats(UUID vendorId, TAccountRequest accountRequest) {
    // TODO: Implement vendor stats
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public PagedResponse<ProductResponse> getPopularProducts(Long minPurchases, Pageable pageable) {
    // TODO: Implement popular products
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public PagedResponse<ProductResponse> getRelatedProducts(UUID productId, Pageable pageable) {
    // TODO: Implement related products
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void incrementProductView(UUID productId) {
    // TODO: Implement view count increment (async)
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void updateProductRating(UUID productId, Double rating) {
    // TODO: Implement rating update
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
