package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.mapper_v2.EnhancedProductMapper;
import com.winnguyen1905.product.core.mapper_v2.InventoryMapper;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.AdminProductService;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

  private final InventoryRepository inventoryRepository;
  private final ProductRepository productRepository;
  private final CacheManager cacheManager;

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<InventoryVm> getAllInventories(Pageable pageable) {
    log.info("Retrieving all inventories with pagination {}", pageable);
    Page<EInventory> inventories = inventoryRepository.findAll(pageable);
    
    List<InventoryVm> inventoryVms = inventories.getContent().stream()
        .map(InventoryMapper::toInventoryVm)
        .collect(Collectors.toList());

    return PagedResponse.<InventoryVm>builder()
        .content(inventoryVms)
        .pageNumber(pageable.getPageNumber())
        .pageSize(pageable.getPageSize())
        .totalElements(inventories.getTotalElements())
        .totalPages(inventories.getTotalPages())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<ProductResponse> getPendingApprovalProducts(Pageable pageable) {
    log.info("Retrieving products pending approval");
    // Find products that are in PENDING_APPROVAL status
    Specification<EProduct> spec = (root, query, cb) -> 
        cb.equal(root.get("status"), ProductStatus.PENDING_APPROVAL);
    
    Page<EProduct> pendingProducts = productRepository.findAll(spec, pageable);
    
    List<ProductResponse> productResponses = pendingProducts.getContent().stream()
        .map(EnhancedProductMapper::toResponse)
        .collect(Collectors.toList());

    return PagedResponse.<ProductResponse>builder()
        .content(productResponses)
        .pageNumber(pageable.getPageNumber())
        .pageSize(pageable.getPageSize())
        .totalElements(pendingProducts.getTotalElements())
        .totalPages(pendingProducts.getTotalPages())
        .build();
  }

  @Override
  @Transactional
  public void approveProduct(UUID productId, Boolean isPublished, String rejectionReason) {
    log.info("Admin approving product: {}, published: {}", productId, isPublished);
    EProduct product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

    if (Boolean.TRUE.equals(isPublished)) {
      product.setStatus(ProductStatus.ACTIVE);
      product.setIsPublished(true);
      // Set rejection reason as null using Map instead of direct field access
      Map<String, Object> specs = new HashMap<>();
      specs.put("rejectionReason", null);
      product.setSpecifications(specs);
    } else {
      product.setStatus(ProductStatus.REJECTED);
      product.setIsPublished(false);
      // Store rejection reason in specifications JSON field
      Map<String, Object> specs = new HashMap<>();
      specs.put("rejectionReason", rejectionReason);
      product.setSpecifications(specs);
    }

    product.setUpdatedDate(Instant.now());
    productRepository.save(product);
    
    // Evict cache for this product
    evictProductCache(productId);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "inventory", key = "#inventoryId")
  })
  public void deleteInventory(UUID inventoryId) {
    log.info("Admin deleting inventory: {}", inventoryId);
    EInventory inventory = inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + inventoryId));

    inventoryRepository.delete(inventory);
  }

  @Override
  public Map<String, Object> getCacheStatistics() {
    log.info("Retrieving cache statistics");
    Map<String, Object> stats = new HashMap<>();
    
    if (cacheManager != null) {
      Collection<String> cacheNames = cacheManager.getCacheNames();
      stats.put("cacheNames", cacheNames);
      
      Map<String, Integer> cacheStats = new HashMap<>();
      cacheNames.forEach(cacheName -> {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
          // Spring doesn't provide built-in cache statistics, so this is approximate
          cacheStats.put(cacheName, cache.toString().length());
        }
      });
      stats.put("approximateSize", cacheStats);
    } else {
      stats.put("status", "Cache manager not available");
    }
    
    return stats;
  }

  @Override
  @CacheEvict(value = {"products", "inventories", "categories", "brands"}, allEntries = true)
  public void clearCache(String cacheName) {
    log.info("Clearing cache: {}", cacheName != null ? cacheName : "all");
    if (cacheName != null && !cacheName.isEmpty()) {
      org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
      }
    } else {
      cacheManager.getCacheNames().forEach(name -> {
        org.springframework.cache.Cache cache = cacheManager.getCache(name);
        if (cache != null) {
          cache.clear();
        }
      });
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<?> getVendorPerformance(Integer days) {
    log.info("Getting vendor performance for the past {} days", days);
    Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
    
    // Example implementation - in real system, this would query analytics data
    // For now, we'll return a simple structure based on product data
    List<EProduct> recentProducts = productRepository.findAll()
        .stream()
        .filter(p -> p.getCreatedDate() != null && p.getCreatedDate().isAfter(startDate))
        .collect(Collectors.toList());
    
    Map<UUID, List<EProduct>> vendorProducts = recentProducts.stream()
        .collect(Collectors.groupingBy(EProduct::getVendorId));
    
    List<Map<String, Object>> results = new ArrayList<>();
    vendorProducts.forEach((vendorId, products) -> {
      Map<String, Object> vendorStats = new HashMap<>();
      vendorStats.put("vendorId", vendorId);
      vendorStats.put("totalProducts", products.size());
      vendorStats.put("activeProducts", products.stream().filter(EProduct::getIsPublished).count());
      
      // More metrics would be added here
      results.add(vendorStats);
    });
    
    return results;
  }
  
  @CacheEvict(value = {"products", "product"}, key = "#productId")
  private void evictProductCache(UUID productId) {
    log.info("Evicting cache for product: {}", productId);
    // Cache eviction is handled by the annotation
  }
} 
