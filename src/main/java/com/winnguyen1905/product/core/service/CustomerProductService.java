package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantByShopVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;

public interface CustomerProductService {
  ProductDetailVm getProductDetail(UUID id);

  List<ProductVariantDetailVm> getProductVariantDetails(UUID productId);

  // To see the product in Cart page
  ProductVariantByShopVm getProductVariantDetails(List<String> productVariantIds);

  // @Cacheable(value = "productSearch", key = "#request")
  PagedResponse<ProductVariantReviewVm> searchProducts(SearchProductRequest request);
}
