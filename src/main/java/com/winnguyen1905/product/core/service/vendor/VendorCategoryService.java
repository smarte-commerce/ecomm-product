package com.winnguyen1905.product.core.service.vendor;

import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AddCateogryRequest;
import com.winnguyen1905.product.core.model.response.CategoryTreeResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VendorCategoryService {
  Mono<CategoryTreeResponse> findAllCategory(UUID shopId);
  Mono<Void> addCategory(UUID shopId, AddCateogryRequest categoryDto);

  // PagedResponse<Category> findAllCategoryWithPageable(UUID shopId, Pageable pageable);

  // Mono<Category> findCategoryById(UUID shopId, UUID categoryId);

  // Mono<Void> updateCategory(UUID shopId, Category category);

  // Mono<Void> deleteManyById(UUID shopId, final List<UUID> ids);
}
