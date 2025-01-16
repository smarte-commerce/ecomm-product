package com.winnguyen1905.product.core.service.customer;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.ProductVariantByShopResponse;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.core.model.response.ProductDetail;
import com.winnguyen1905.product.core.model.response.ProductVariantReview;

import reactor.core.publisher.Mono;

public interface CustomerProductService {
  Mono<ProductDetail> getProductDetail(UUID id);
  ProductVariantByShopResponse getProductVariantDetails(List<String> productVariantIds);
  Mono<PagedResponse<ProductVariantReview>> searchProducts(SearchProductRequest request);
}
