package com.winnguyen1905.product.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EProductImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<EProductImage, UUID> {
    
    /**
     * Find images by product ID with pagination, excluding deleted images
     */
    Page<EProductImage> findByProductIdAndIsDeletedFalse(UUID productId, Pageable pageable);
    
    /**
     * Find image by ID, excluding deleted images
     */
    Optional<EProductImage> findByIdAndIsDeletedFalse(UUID id);
    
    /**
     * Find image by ID and product ID, excluding deleted images
     */
    Optional<EProductImage> findByIdAndProductIdAndIsDeletedFalse(UUID id, UUID productId);
    
    /**
     * Find primary images by product ID and variant ID
     */
    @Query("SELECT i FROM EProductImage i WHERE i.product.id = :productId AND i.variant.id = :variantId AND i.isPrimary = true AND i.isDeleted = false")
    List<EProductImage> findByProductIdAndVariantIdAndIsPrimaryTrueAndIsDeletedFalse(@Param("productId") UUID productId, @Param("variantId") UUID variantId);
    
    /**
     * Find primary product-level images (where variant is null)
     */
    @Query("SELECT i FROM EProductImage i WHERE i.product.id = :productId AND i.variant IS NULL AND i.isPrimary = true AND i.isDeleted = false")
    List<EProductImage> findByProductIdAndVariantIsNullAndIsPrimaryTrueAndIsDeletedFalse(@Param("productId") UUID productId);
    
    /**
     * Find all images by product ID, excluding deleted
     */
    List<EProductImage> findByProductIdAndIsDeletedFalse(UUID productId);
    
    /**
     * Find images by product ID and variant ID
     */
    List<EProductImage> findByProductIdAndVariantIdAndIsDeletedFalse(UUID productId, UUID variantId);
    
    /**
     * Find images by variant ID only
     */
    List<EProductImage> findByVariantIdAndIsDeletedFalse(UUID variantId);
    
    /**
     * Count images by product ID
     */
    long countByProductIdAndIsDeletedFalse(UUID productId);
    
    /**
     * Find primary image for a product
     */
    @Query("SELECT i FROM EProductImage i WHERE i.product.id = :productId AND i.isPrimary = true AND i.isDeleted = false ORDER BY i.displayOrder ASC")
    Optional<EProductImage> findPrimaryImageByProductId(@Param("productId") UUID productId);
    
    /**
     * Find primary image for a product variant
     */
    @Query("SELECT i FROM EProductImage i WHERE i.variant.id = :variantId AND i.isPrimary = true AND i.isDeleted = false ORDER BY i.displayOrder ASC")
    Optional<EProductImage> findPrimaryImageByVariantId(@Param("variantId") UUID variantId);
    
    /**
     * Find images by vendor ID with pagination
     */
    @Query("SELECT i FROM EProductImage i WHERE i.vendorId = :vendorId AND i.isDeleted = false")
    Page<EProductImage> findByVendorIdAndIsDeletedFalse(@Param("vendorId") UUID vendorId, Pageable pageable);
    
    /**
     * Check if image exists by storage key
     */
    boolean existsByStorageKeyAndIsDeletedFalse(String storageKey);
    
    /**
     * Find image by storage key
     */
    Optional<EProductImage> findByStorageKeyAndIsDeletedFalse(String storageKey);
    
    /**
     * Bulk update primary status
     */
    @Query("UPDATE EProductImage i SET i.isPrimary = false WHERE i.product.id = :productId AND i.id != :excludeImageId AND i.isDeleted = false")
    void unsetPrimaryImagesForProduct(@Param("productId") UUID productId, @Param("excludeImageId") UUID excludeImageId);
    
    /**
     * Bulk update primary status for variant
     */
    @Query("UPDATE EProductImage i SET i.isPrimary = false WHERE i.variant.id = :variantId AND i.id != :excludeImageId AND i.isDeleted = false")
    void unsetPrimaryImagesForVariant(@Param("variantId") UUID variantId, @Param("excludeImageId") UUID excludeImageId);
}
