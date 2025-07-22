package com.winnguyen1905.product.core.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.model.request.AddCateogryRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.viewmodel.CategoryTreeVm;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.VendorCategoryService;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.secure.TAccountRequest;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorCategoryServiceImpl implements VendorCategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  public List<?> getAllCategories(boolean includeInactive) {
    log.info("Getting all categories, includeInactive: {}", includeInactive);
    // Implementation details to be added
    return Collections.emptyList();
  }

  @Override
  public Object getCategoryById(UUID categoryId) {
    log.info("Getting category with ID: {}", categoryId);
    // Implementation details to be added
    return null;
  }

  @Override
  public Object createCategory(Object categoryRequest, TAccountRequest accountRequest) {
    log.info("Creating category by user: {}", accountRequest.id());
    // Implementation details to be added
    return null;
  }

  @Override
  public Object updateCategory(UUID categoryId, Object categoryRequest, TAccountRequest accountRequest) {
    log.info("Updating category: {} by user: {}", categoryId, accountRequest.id());
    // Implementation details to be added
    return null;
  }

  @Override
  public void deleteCategory(UUID categoryId, TAccountRequest accountRequest) {
    log.info("Deleting category: {} by user: {}", categoryId, accountRequest.id());
    // Implementation details to be added
  }

  @Override
  public CategoryTreeVm getCategoryTree() {
    log.info("Getting category tree");
    // Implementation details to be added
    return null;
  }

  @Override
  public Object moveCategory(UUID categoryId, UUID parentId, TAccountRequest accountRequest) {
    log.info("Moving category: {} to parent: {} by user: {}", categoryId, parentId, accountRequest.id());
    // Implementation details to be added
    return null;
  }

  @Override
  public PagedResponse<?> searchCategories(String query, Pageable pageable) {
    log.info("Searching categories with query: {}", query);
    // Implementation details to be added
    return null;
  }

  @Override
  public PagedResponse<ProductResponse> getCategoryProducts(UUID categoryId, Pageable pageable) {
    log.info("Getting products for category: {}", categoryId);
    // Implementation details to be added
    return null;
  }

  @Override
  public CategoryTreeVm findAllShopCategory(UUID shopId) {
    log.info("Finding all shop categories for shop: {}", shopId);
    // Implementation will be added later
    return null;
  }

  @Override
  public void addCategory(TAccountRequest accountRequest, AddCateogryRequest categoryDto) {
    log.info("Adding category for user: {}", accountRequest.id());
    // Implementation will be added later
  }
}
