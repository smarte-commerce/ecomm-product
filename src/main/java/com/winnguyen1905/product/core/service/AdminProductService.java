package com.winnguyen1905.product.core.service;

import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.InventoryVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.secure.TAccountRequest;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Administrative service for managing products and system operations
 */
public interface AdminProductService {
  /**
   * Get all inventories with pagination
   */
  PagedResponse<InventoryVm> getAllInventories(Pageable pageable);

  /**
   * Get all products pending approval with pagination
   */
  PagedResponse<ProductResponse> getPendingApprovalProducts(Pageable pageable);

  /**
   * Approve or reject a product
   */
  void approveProduct(UUID productId, Boolean isPublished, String rejectionReason);

  /**
   * Force delete an inventory
   */
  void deleteInventory(UUID inventoryId);

  /**
   * Get cache statistics
   */
  Map<String, Object> getCacheStatistics();

  /**
   * Clear cache (all or specific)
   */
  void clearCache(String cacheName);

  /**
   * Get vendor performance metrics
   */
  List<?> getVendorPerformance(Integer days);
}
