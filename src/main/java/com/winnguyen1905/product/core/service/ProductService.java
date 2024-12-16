package com.winnguyen1905.product.core.service;

import java.util.UUID;

import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;

import reactor.core.publisher.Mono;

public interface ProductService {
  Mono<Product> findProductById(UUID id);
  Mono<Product> addProduct(UUID shopId, AddProductRequest productRequest);

  // List<Product> handleChangeProductStatus(UUID shopId, List<UUID> ids);

  // PagedResponse<Product> handleGetAllProducts(SearchProductRequest
  // productSearchRequest, Pageable pageable);

  // void handleDeleteProducts(UUID shopId, List<UUID> ids);
}
