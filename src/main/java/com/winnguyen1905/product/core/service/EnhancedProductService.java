package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.model.request.CreateProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.secure.TAccountRequest;

/**
 * Enhanced Product Service Interface
 * Comprehensive product management service for multi-vendor ecommerce platform
 */
public interface EnhancedProductService {

    // ================== CRUD OPERATIONS ==================

    /**
     * Tạo sản phẩm mới với multi-vendor support
     */
    ProductResponse createProduct(CreateProductRequest request, TAccountRequest accountRequest);

    /**
     * Cập nhật sản phẩm với optimistic locking
     */
    ProductResponse updateProduct(UUID productId, UpdateProductRequest request, TAccountRequest accountRequest);

    /**
     * Lấy chi tiết sản phẩm với caching
     */
    ProductResponse getProduct(UUID productId, TAccountRequest accountRequest);

    /**
     * Lấy chi tiết sản phẩm công khai (không cần authentication)
     */
    ProductResponse getPublicProduct(UUID productId);

    /**
     * Xóa mềm sản phẩm (soft delete)
     */
    void deleteProduct(UUID productId, TAccountRequest accountRequest);

    /**
     * Khôi phục sản phẩm đã xóa
     */
    ProductResponse restoreProduct(UUID productId, TAccountRequest accountRequest);

    // ================== VENDOR OPERATIONS ==================

    /**
     * Lấy danh sách sản phẩm theo vendor
     */
    PagedResponse<ProductResponse> getVendorProducts(UUID vendorId, ProductStatus status, Pageable pageable, TAccountRequest accountRequest);

    /**
     * Lấy danh sách sản phẩm theo shop
     */
    PagedResponse<ProductResponse> getShopProducts(UUID shopId, Pageable pageable);

    /**
     * Tìm kiếm sản phẩm với Elasticsearch
     */
    PagedResponse<ProductResponse> searchProducts(SearchProductRequest request, TAccountRequest accountRequest);

    // ================== BULK OPERATIONS ==================

    /**
     * Cập nhật trạng thái nhiều sản phẩm
     */
    void updateProductsStatus(List<UUID> productIds, ProductStatus status, TAccountRequest accountRequest);

    /**
     * Publish/unpublish nhiều sản phẩm
     */
    void updateProductsPublishStatus(List<UUID> productIds, Boolean published, TAccountRequest accountRequest);

    /**
     * Xóa nhiều sản phẩm
     */
    void deleteProducts(List<UUID> productIds, TAccountRequest accountRequest);

    /**
     * Import sản phẩm hàng loạt
     */
    List<ProductResponse> bulkImportProducts(List<CreateProductRequest> requests, TAccountRequest accountRequest);

    // ================== CATEGORY & BRAND MANAGEMENT ==================

    /**
     * Lấy sản phẩm theo category
     */
    PagedResponse<ProductResponse> getProductsByCategory(UUID categoryId, UUID vendorId, Pageable pageable, TAccountRequest accountRequest);

    /**
     * Lấy sản phẩm theo brand
     */
    PagedResponse<ProductResponse> getProductsByBrand(UUID brandId, UUID vendorId, Pageable pageable, TAccountRequest accountRequest);

    /**
     * Cập nhật category cho sản phẩm
     */
    ProductResponse updateProductCategory(UUID productId, UUID categoryId, TAccountRequest accountRequest);

    /**
     * Cập nhật brand cho sản phẩm
     */
    ProductResponse updateProductBrand(UUID productId, UUID brandId, TAccountRequest accountRequest);

    // ================== INVENTORY INTEGRATION ==================

    /**
     * Đồng bộ inventory với Elasticsearch
     */
    void syncProductInventory(UUID productId);

    /**
     * Kiểm tra và cập nhật low stock products
     */
    List<ProductResponse> checkLowStockProducts(UUID vendorId);

    /**
     * Cập nhật stock cho product variants
     */
    void updateProductStock(UUID productId, TAccountRequest accountRequest);

    // ================== SEO & SLUG MANAGEMENT ==================

    /**
     * Tạo slug unique cho sản phẩm
     */
    String generateUniqueSlug(String name, UUID vendorId);

    /**
     * Lấy sản phẩm theo slug
     */
    ProductResponse getProductBySlug(String slug, UUID vendorId);

    /**
     * Cập nhật SEO metadata
     */
    ProductResponse updateProductSEO(UUID productId, String metaTitle, String metaDescription, String metaKeywords, TAccountRequest accountRequest);

    // ================== ANALYTICS & REPORTING ==================

    /**
     * Lấy thống kê sản phẩm theo vendor
     */
    VendorProductStats getVendorProductStats(UUID vendorId, TAccountRequest accountRequest);

    /**
     * Lấy sản phẩm hot (nhiều lượt xem/mua)
     */
    PagedResponse<ProductResponse> getPopularProducts(Long minPurchases, Pageable pageable);

    /**
     * Lấy sản phẩm liên quan
     */
    PagedResponse<ProductResponse> getRelatedProducts(UUID productId, Pageable pageable);

    /**
     * Tăng view count cho sản phẩm
     */
    void incrementProductView(UUID productId);

    /**
     * Cập nhật rating cho sản phẩm
     */
    void updateProductRating(UUID productId, Double rating);

    // ================== CACHE MANAGEMENT ==================

    /**
     * Xóa cache cho sản phẩm
     */
    void evictProductCache(UUID productId);

    /**
     * Xóa toàn bộ cache sản phẩm của vendor
     */
    void evictVendorProductsCache(UUID vendorId);

    /**
     * Warm up cache cho sản phẩm hot
     */
    void warmUpProductCache();

    // ================== VALIDATION ==================

    /**
     * Validate quyền truy cập sản phẩm
     */
    boolean hasProductAccess(UUID productId, TAccountRequest accountRequest);

    /**
     * Validate vendor ownership
     */
    boolean isProductOwner(UUID productId, UUID vendorId);

    /**
     * Validate business rules
     */
    void validateProductCreation(CreateProductRequest request, TAccountRequest accountRequest);

    void validateProductUpdate(UUID productId, UpdateProductRequest request, TAccountRequest accountRequest);

    // ================== DATA MODELS ==================

    /**
     * Vendor Product Statistics
     */
    record VendorProductStats(
        UUID vendorId,
        long totalProducts,
        long publishedProducts,
        long draftProducts,
        long activeProducts,
        long inactiveProducts,
        long deletedProducts,
        long lowStockProducts,
        double averageRating,
        long totalViews,
        long totalPurchases
    ) {}
} 
