package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.product.core.model.request.AddCateogryRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.CategoryTreeVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.secure.TAccountRequest;

/**
 * Service for category management operations
 */
public interface VendorCategoryService {
  /**
   * Get all categories with hierarchical structure
   */
  List<?> getAllCategories(boolean includeInactive);

  /**
   * Get a category by its ID
   */
  Object getCategoryById(UUID categoryId);

  /**
   * Create a new category
   */
  Object createCategory(Object categoryRequest, TAccountRequest accountRequest);

  /**
   * Update an existing category
   */
  Object updateCategory(UUID categoryId, Object categoryRequest, TAccountRequest accountRequest);

  /**
   * Delete a category
   */
  void deleteCategory(UUID categoryId, TAccountRequest accountRequest);

  /**
   * Get hierarchical category tree
   */
  CategoryTreeVm getCategoryTree();

  /**
   * Move a category to a new parent
   */
  Object moveCategory(UUID categoryId, UUID parentId, TAccountRequest accountRequest);

  /**
   * Search categories by name or other criteria
   */
  PagedResponse<?> searchCategories(String query, Pageable pageable);

  /**
   * Get products belonging to a category
   */
  PagedResponse<ProductResponse> getCategoryProducts(UUID categoryId, Pageable pageable);

  /**
   * Legacy method - find all shop categories
   */
  CategoryTreeVm findAllShopCategory(UUID shopId);

  /**
   * Legacy method - add a category
   */
  void addCategory(TAccountRequest accountRequest, AddCateogryRequest categoryDto);
}
