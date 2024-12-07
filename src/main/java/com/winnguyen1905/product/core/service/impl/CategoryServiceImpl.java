package com.winnguyen1905.product.core.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.model.response.Category;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.core.service.CategoryService;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final ModelMapper mapper;
  private final CategoryRepository categoryRepository;

  @Override
  public Flux<Category> findAllCategory(UUID shopId) {
    return Flux.fromIterable(this.categoryRepository.findAllByShopId(shopId))
        .map(category -> this.mapper.map(category, Category.class))
        .onErrorResume(throwable -> {
          log.error("Some error happed: " + throwable.getMessage());
          return Flux.empty();
        });
  }

  @Override
  public PagedResponse<Category> findAllCategoryWithPageable(UUID shopId, Pageable pageable) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'findAllCategoryWithPageable'");
  }

  @Override
  public Mono<Category> findCategoryById(UUID shopId, UUID categoryId) {
    return Mono.fromCallable(() -> this.categoryRepository.findByIdAndShopId(categoryId, shopId))
        .map(category -> this.mapper.map(categoryId, Category.class));
  }

  @Override
  public Mono<Void> updateCategory(UUID shopId, Category category) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'updateCategory'");
  }

  @Override
  public Mono<Void> deleteManyById(UUID shopId, List<UUID> ids) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteManyById'");
  }

  @Override
  public Mono<Category> addCategory(UUID shopId, Category category) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addCategory'");
  }

}
