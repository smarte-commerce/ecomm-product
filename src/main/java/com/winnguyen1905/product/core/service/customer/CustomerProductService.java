package com.winnguyen1905.product.core.service.customer;

import java.util.UUID;

import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.PagedResponse;

import reactor.core.publisher.Mono;

public interface CustomerProductService {
  Mono<Product> findProductById(UUID id);
  Mono<PagedResponse<ProductVariant>> searchProducts(SearchProductRequest request);
}
