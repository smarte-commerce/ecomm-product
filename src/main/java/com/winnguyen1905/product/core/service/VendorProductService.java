package com.winnguyen1905.product.core.service;

import java.util.UUID;

import com.winnguyen1905.product.secure.TAccountRequest;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.persistance.entity.EProduct;

public interface VendorProductService {
  void persistProductVariants(EProduct product);
  void addProduct(TAccountRequest accountRequest, AddProductRequest productRequest);
  void updateProduct(TAccountRequest accountRequest, UpdateProductRequest updateProductRequest);
  void deleteProduct(TAccountRequest accountRequest, UUID productId);

  // List<Product> handleChangeProductStatus(UUID shopId, List<UUID> ids);

  // PagedResponse<Product> handleGetAllProducts(SearchProductRequest
  // productSearchRequest, Pageable pageable);
  // @CacheEvict(value = "productSearch", allEntries = true)
  // void handleDeleteProducts(UUID shopId, List<UUID> ids);
}
