package com.winnguyen1905.product.core.service.vendor;

import java.util.UUID;

import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.persistance.entity.EProduct;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VendorProductService {
  Mono<Void> persistProductVariants(EProduct product);
  Mono<Product> addProduct(UUID shopId, AddProductRequest productRequest);

  // List<Product> handleChangeProductStatus(UUID shopId, List<UUID> ids);

  // PagedResponse<Product> handleGetAllProducts(SearchProductRequest
  // productSearchRequest, Pageable pageable);

  // void handleDeleteProducts(UUID shopId, List<UUID> ids);
}
