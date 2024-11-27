package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.PagedResponse;

public interface ProductService {
  Product handleAddProduct(UUID shopId, AddProductRequest productRequest);

  List<Product> handleChangeProductStatus( UUID shopId, List<UUID> ids);

  PagedResponse<Product> handleGetAllProducts(SearchProductRequest productSearchRequest, Pageable pageable);

  Product handleGetProduct(UUID id);

  void handleDeleteProducts(UUID shopId, List<UUID> ids);
}
