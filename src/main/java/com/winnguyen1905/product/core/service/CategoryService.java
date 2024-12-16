package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.product.core.model.response.Category;
import com.winnguyen1905.product.core.model.response.PagedResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {
  Flux<Category> findAllCategory(UUID shopId);
  Mono<Category> addCategory(UUID shopId, Category category);
  // PagedResponse<Category> findAllCategoryWithPageable(UUID shopId, Pageable pageable);

  // Mono<Category> findCategoryById(UUID shopId, UUID categoryId);

  // Mono<Void> updateCategory(UUID shopId, Category category);

  // Mono<Void> deleteManyById(UUID shopId, final List<UUID> ids);
}
