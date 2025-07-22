package com.winnguyen1905.product.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.secure.RegionPartition;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface EnhancedProductRepository extends JpaRepository<EProduct, UUID>, JpaSpecificationExecutor<EProduct> {

    // ================== BASIC CRUD WITH CACHING ==================

    @Override
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    Optional<EProduct> findById(UUID id);

    @Override
    @CachePut(value = "products", key = "#result.id")
    <S extends EProduct> S save(S entity);

    @Override
    @CacheEvict(value = "products", key = "#id")
    void deleteById(UUID id);

    // ================== VENDOR-SPECIFIC QUERIES ==================

    @Cacheable(value = "products", key = "'vendor:' + #vendorId + ':' + #pageable.pageNumber")
    @Query("SELECT p FROM EProduct p WHERE p.vendorId = :vendorId AND p.isDeleted = false")
    Page<EProduct> findByVendorId(@Param("vendorId") UUID vendorId, Pageable pageable);

    @Cacheable(value = "products", key = "'shop:' + #shopId + ':published:' + #pageable.pageNumber")
    Page<EProduct> findAllByShopIdAndIsPublishedTrue(UUID shopId, Pageable pageable);

    @Query("SELECT p FROM EProduct p WHERE p.shopId = :shopId AND p.status = :status AND p.isDeleted = false")
    Page<EProduct> findByShopIdAndStatus(@Param("shopId") UUID shopId, @Param("status") ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM EProduct p WHERE p.vendorId = :vendorId AND p.region = :region AND p.isDeleted = false")
    Page<EProduct> findByVendorIdAndRegion(@Param("vendorId") UUID vendorId, @Param("region") RegionPartition region, Pageable pageable);

    // ================== OPTIMISTIC LOCKING ==================

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM EProduct p WHERE p.id = :id")
    Optional<EProduct> findByIdWithOptimisticLock(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM EProduct p WHERE p.id = :id")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000")})
    Optional<EProduct> findByIdWithPessimisticLock(@Param("id") UUID id);

    // ================== BATCH OPERATIONS ==================

    @Modifying
    @CacheEvict(value = "products", allEntries = true)
    @Query("UPDATE EProduct p SET p.isDeleted = true WHERE p.id IN :ids AND p.vendorId = :vendorId")
    int softDeleteByIdsAndVendor(@Param("ids") List<UUID> ids, @Param("vendorId") UUID vendorId);

    @Query("SELECT p FROM EProduct p WHERE p.id IN :ids AND p.vendorId = :vendorId AND p.isDeleted = false")
    List<EProduct> findByIdsAndVendor(@Param("ids") List<UUID> ids, @Param("vendorId") UUID vendorId);

    // ================== PUBLISHED PRODUCTS ==================

    @Cacheable(value = "products", key = "'published:' + #id")
    Optional<EProduct> findByIdAndIsPublishedTrue(UUID id);

    @Query("SELECT p FROM EProduct p WHERE p.isPublished = true AND p.status = 'ACTIVE' AND p.isDeleted = false")
    Page<EProduct> findAllPublishedAndActive(Pageable pageable);

    // ================== CATEGORY & BRAND FILTERING ==================

    @Query("SELECT p FROM EProduct p WHERE p.category.id = :categoryId AND p.vendorId = :vendorId AND p.isDeleted = false")
    Page<EProduct> findByCategoryAndVendor(@Param("categoryId") UUID categoryId, @Param("vendorId") UUID vendorId, Pageable pageable);

    @Query("SELECT p FROM EProduct p WHERE p.brand.id = :brandId AND p.vendorId = :vendorId AND p.isDeleted = false")
    Page<EProduct> findByBrandAndVendor(@Param("brandId") UUID brandId, @Param("vendorId") UUID vendorId, Pageable pageable);

    // ================== SEARCH & FILTERING ==================

    @Query("SELECT p FROM EProduct p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:vendorId IS NULL OR p.vendorId = :vendorId) AND " +
           "(:region IS NULL OR p.region = :region) AND " +
           "p.isDeleted = false")
    Page<EProduct> searchProducts(@Param("name") String name,
                                  @Param("productType") ProductType productType,
                                  @Param("vendorId") UUID vendorId,
                                  @Param("region") RegionPartition region,
                                  Pageable pageable);

    // ================== PARTITION-FIRST SEARCH ==================

    @Query("SELECT p FROM EProduct p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:vendorId IS NULL OR p.vendorId = :vendorId) AND " +
           "p.region = :region AND " +
           "p.isPublished = true AND p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.purchaseCount DESC, p.ratingAverage DESC")
    Page<EProduct> searchProductsInPartition(@Param("name") String name,
                                           @Param("productType") ProductType productType,
                                           @Param("vendorId") UUID vendorId,
                                           @Param("region") RegionPartition region,
                                           Pageable pageable);

    @Query("SELECT p FROM EProduct p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:vendorId IS NULL OR p.vendorId = :vendorId) AND " +
           "p.region != :excludeRegion AND " +
           "p.isPublished = true AND p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.ratingAverage DESC, p.purchaseCount DESC")
    Page<EProduct> searchProductsInOtherPartitions(@Param("name") String name,
                                                  @Param("productType") ProductType productType,
                                                  @Param("vendorId") UUID vendorId,
                                                  @Param("excludeRegion") RegionPartition excludeRegion,
                                                  Pageable pageable);

    @Query("SELECT COUNT(p) FROM EProduct p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:vendorId IS NULL OR p.vendorId = :vendorId) AND " +
           "p.region = :region AND " +
           "p.isPublished = true AND p.status = 'ACTIVE' AND p.isDeleted = false")
    long countProductsInPartition(@Param("name") String name,
                                 @Param("productType") ProductType productType,
                                 @Param("vendorId") UUID vendorId,
                                 @Param("region") RegionPartition region);

    // ================== ANALYTICS QUERIES ==================

    @Query("SELECT COUNT(p) FROM EProduct p WHERE p.vendorId = :vendorId AND p.isDeleted = false")
    long countByVendorId(@Param("vendorId") UUID vendorId);

    @Query("SELECT COUNT(p) FROM EProduct p WHERE p.vendorId = :vendorId AND p.status = :status AND p.isDeleted = false")
    long countByVendorIdAndStatus(@Param("vendorId") UUID vendorId, @Param("status") ProductStatus status);

    @Query("SELECT p.status, COUNT(p) FROM EProduct p WHERE p.vendorId = :vendorId AND p.isDeleted = false GROUP BY p.status")
    List<Object[]> getProductStatusCounts(@Param("vendorId") UUID vendorId);

    // ================== BULK OPERATIONS ==================

    @Modifying
    @CacheEvict(value = "products", allEntries = true)
    @Query("UPDATE EProduct p SET p.status = :status WHERE p.id IN :ids AND p.vendorId = :vendorId")
    int updateStatusByIdsAndVendor(@Param("ids") List<UUID> ids, @Param("status") ProductStatus status, @Param("vendorId") UUID vendorId);

    @Modifying
    @CacheEvict(value = "products", allEntries = true)
    @Query("UPDATE EProduct p SET p.isPublished = :published WHERE p.id IN :ids AND p.vendorId = :vendorId")
    int updatePublishedByIdsAndVendor(@Param("ids") List<UUID> ids, @Param("published") Boolean published, @Param("vendorId") UUID vendorId);

    // ================== SLUG & SEO ==================

    Optional<EProduct> findBySlug(String slug);

    @Query("SELECT p FROM EProduct p WHERE p.slug = :slug AND p.vendorId = :vendorId AND p.isDeleted = false")
    Optional<EProduct> findBySlugAndVendor(@Param("slug") String slug, @Param("vendorId") UUID vendorId);

    boolean existsBySlugAndVendorId(String slug, UUID vendorId);

    // ================== FEATURED & RECOMMENDATIONS ==================

    @Query("SELECT p FROM EProduct p WHERE p.isPublished = true AND p.status = 'ACTIVE' AND " +
           "p.purchaseCount > :minPurchases AND p.isDeleted = false ORDER BY p.purchaseCount DESC")
    Page<EProduct> findPopularProducts(@Param("minPurchases") Long minPurchases, Pageable pageable);

    @Query("SELECT p FROM EProduct p WHERE p.category.id = :categoryId AND p.id != :excludeId AND " +
           "p.isPublished = true AND p.status = 'ACTIVE' AND p.isDeleted = false ORDER BY p.ratingAverage DESC")
    Page<EProduct> findRelatedProducts(@Param("categoryId") UUID categoryId, @Param("excludeId") UUID excludeId, Pageable pageable);

    // ================== PERFORMANCE MONITORING ==================

    @Query("SELECT p FROM EProduct p WHERE p.viewCount > :threshold AND p.isDeleted = false ORDER BY p.viewCount DESC")
    Page<EProduct> findHighViewProducts(@Param("threshold") Long threshold, Pageable pageable);

    @Query("SELECT p FROM EProduct p WHERE p.lowStockThreshold IS NOT NULL AND " +
           "SIZE(p.variants) > 0 AND p.isDeleted = false")
    List<EProduct> findProductsNeedingInventoryCheck();
} 
