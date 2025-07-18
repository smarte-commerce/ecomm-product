package com.winnguyen1905.product.service;

import com.winnguyen1905.product.config.RegionalDataSourceConfiguration;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.BrandRepository;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.persistance.repository.EnhancedProductRepository;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;
import com.winnguyen1905.product.secure.RegionPartition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that demonstrates smart regional query patterns.
 * Shows how to effectively use region filters for data partitioning.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionalQueryService {

  private final EnhancedProductRepository productRepository;
  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;
  private final ProductVariantRepository variantRepository;

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Smart method to find products with automatic region filtering.
   * Uses the region set by AccountRequestArgumentResolver automatically.
   */
  public Page<EProduct> findProductsInCurrentRegion(Pageable pageable) {
    // The region filter is automatically applied by RegionHibernateFilterConfigurer
    // This query will only return products from the user's region
    log.debug("Finding products in current region with automatic filtering");
    return productRepository.findAllPublishedAndActive(pageable);
  }

  /**
   * Find products for a specific region (override current region)
   */
  public Page<EProduct> findProductsInSpecificRegion(RegionPartition region, Pageable pageable) {
    log.debug("Finding products specifically in region: {}", region.getCode());

    return withRegionFilter(region, () -> {
      return productRepository.findAllPublishedAndActive(pageable);
    });
  }

  /**
   * Find products across all regions (bypass region filtering)
   */
  public Page<EProduct> findProductsInAllRegions(Pageable pageable) {
    log.debug("Finding products across all regions (bypassing region filter)");

    return withoutRegionFilter(() -> {
      return productRepository.findAllPublishedAndActive(pageable);
    });
  }

  /**
   * Smart vendor-specific product query with region isolation
   */
  public Page<EProduct> findVendorProductsInCurrentRegion(UUID vendorId, Pageable pageable) {
    log.debug("Finding vendor {} products in current region", vendorId);

    // This automatically uses both region and vendor filters
    return withVendorFilter(vendorId, () -> {
      return productRepository.findByVendorId(vendorId, pageable);
    });
  }

  /**
   * Complex query with manual region control
   */
  public List<EProduct> findPopularProductsInRegion(RegionPartition region, int minPurchases) {
    log.debug("Finding popular products in region {} with min purchases: {}", region.getCode(), minPurchases);

    return withRegionFilter(region, () -> {
      Query query = entityManager.createQuery(
          "SELECT p FROM EProduct p WHERE p.purchaseCount >= :minPurchases AND p.isPublished = true ORDER BY p.purchaseCount DESC",
          EProduct.class);
      query.setParameter("minPurchases", (long) minPurchases);
      query.setMaxResults(50); // Limit results
      return query.getResultList();
    });
  }

  /**
   * Regional analytics query - count products by region
   */
  public Long countProductsInCurrentRegion() {
    log.debug("Counting products in current region");

    Query query = entityManager.createQuery(
        "SELECT COUNT(p) FROM EProduct p WHERE p.isDeleted = false");
    return (Long) query.getSingleResult();
  }

  /**
   * Cross-region comparison (for admin use)
   */
  public RegionalStats getRegionalProductStats() {
    log.debug("Getting product statistics across all regions");

    return withoutRegionFilter(() -> {
      Query usQuery = entityManager.createQuery(
          "SELECT COUNT(p) FROM EProduct p WHERE p.region = :region AND p.isDeleted = false");
      usQuery.setParameter("region", RegionPartition.US);
      Long usCount = (Long) usQuery.getSingleResult();

      Query euQuery = entityManager.createQuery(
          "SELECT COUNT(p) FROM EProduct p WHERE p.region = :region AND p.isDeleted = false");
      euQuery.setParameter("region", RegionPartition.EU);
      Long euCount = (Long) euQuery.getSingleResult();

      Query asiaQuery = entityManager.createQuery(
          "SELECT COUNT(p) FROM EProduct p WHERE p.region = :region AND p.isDeleted = false");
      asiaQuery.setParameter("region", RegionPartition.ASIA);
      Long asiaCount = (Long) asiaQuery.getSingleResult();

      return new RegionalStats(usCount, euCount, asiaCount);
    });
  }

  /**
   * Find categories with regional filtering
   */
  public List<ECategory> findCategoriesInCurrentRegion() {
    log.debug("Finding categories in current region");
    return categoryRepository.findAll();
  }

  /**
   * Find brands with regional filtering
   */
  public List<EBrand> findBrandsInCurrentRegion() {
    log.debug("Finding brands in current region");
    return brandRepository.findAll();
  }

  /**
   * Regional inventory check
   */
  public List<EProductVariant> findLowStockVariantsInCurrentRegion(int threshold) {
    log.debug("Finding low stock variants in current region with threshold: {}", threshold);

    Query query = entityManager.createQuery(
        "SELECT v FROM EProductVariant v WHERE v.inventoryQuantity <= :threshold AND v.trackInventory = true AND v.isActive = true",
        EProductVariant.class);
    query.setParameter("threshold", threshold);
    return query.getResultList();
  }

  // ===================== UTILITY METHODS =====================

  /**
   * Execute a query with specific region filter
   */
  public <T> T withRegionFilter(RegionPartition region, RegionalQueryCallback<T> callback) {
    Session session = entityManager.unwrap(Session.class);
    Filter regionFilter = null;

    try {
      // Save current filter state
      boolean wasEnabled = session.getEnabledFilter("regionFilter") != null;

      // Enable region filter for specific region
      regionFilter = session.enableFilter("regionFilter");
      regionFilter.setParameter("region", region.getCode());

      log.debug("Applied region filter for: {}", region.getCode());

      // Execute callback with filter active
      return callback.execute();

    } finally {
      // Clean up - disable the filter
      if (regionFilter != null) {
        try {
          session.disableFilter("regionFilter");
          log.debug("Disabled region filter");
        } catch (Exception e) {
          log.warn("Error disabling region filter: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * Execute a query without region filtering
   */
  public <T> T withoutRegionFilter(RegionalQueryCallback<T> callback) {
    Session session = entityManager.unwrap(Session.class);
    boolean wasRegionFilterEnabled = false;
    boolean wasVendorFilterEnabled = false;

    try {
      // Save current filter states and disable them
      Filter regionFilter = session.getEnabledFilter("regionFilter");
      if (regionFilter != null) {
        wasRegionFilterEnabled = true;
        session.disableFilter("regionFilter");
        log.debug("Temporarily disabled region filter");
      }

      Filter vendorFilter = session.getEnabledFilter("vendorFilter");
      if (vendorFilter != null) {
        wasVendorFilterEnabled = true;
        session.disableFilter("vendorFilter");
        log.debug("Temporarily disabled vendor filter");
      }

      // Execute callback without filters
      return callback.execute();

    } finally {
      // Restore original filter states
      try {
        if (wasRegionFilterEnabled) {
          RegionPartition currentRegion = RegionalDataSourceConfiguration.RegionalContext.getCurrentRegion();
          if (currentRegion != null) {
            session.enableFilter("regionFilter").setParameter("region", currentRegion.getCode());
            log.debug("Restored region filter for: {}", currentRegion.getCode());
          }
        }

        if (wasVendorFilterEnabled) {
          // Note: This would need vendor ID from context
          // For now, we'll leave it disabled
        }
      } catch (Exception e) {
        log.warn("Error restoring filters: {}", e.getMessage());
      }
    }
  }

  /**
   * Execute a query with vendor filter
   */
  public <T> T withVendorFilter(UUID vendorId, RegionalQueryCallback<T> callback) {
    Session session = entityManager.unwrap(Session.class);
    Filter vendorFilter = null;

    try {
      vendorFilter = session.enableFilter("vendorFilter");
      vendorFilter.setParameter("vendorId", vendorId.toString());

      log.debug("Applied vendor filter for: {}", vendorId);

      return callback.execute();

    } finally {
      if (vendorFilter != null) {
        try {
          session.disableFilter("vendorFilter");
          log.debug("Disabled vendor filter");
        } catch (Exception e) {
          log.warn("Error disabling vendor filter: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * Get current region from context
   */
  public Optional<RegionPartition> getCurrentRegion() {
    return Optional.ofNullable(RegionalDataSourceConfiguration.RegionalContext.getCurrentRegion());
  }

  /**
   * Check if region filter is currently active
   */
  public boolean isRegionFilterActive() {
    try {
      Session session = entityManager.unwrap(Session.class);
      return session.getEnabledFilter("regionFilter") != null;
    } catch (Exception e) {
      log.warn("Error checking region filter status: {}", e.getMessage());
      return false;
    }
  }

  // ===================== CALLBACK INTERFACES =====================

  @FunctionalInterface
  public interface RegionalQueryCallback<T> {
    T execute();
  }

  // ===================== DATA CLASSES =====================

  public record RegionalStats(Long usCount, Long euCount, Long asiaCount) {
    public Long getTotalCount() {
      return usCount + euCount + asiaCount;
    }
  }
}
