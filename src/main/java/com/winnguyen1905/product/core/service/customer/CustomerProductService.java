package com.winnguyen1905.product.core.service.customer;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.winnguyen1905.product.core.model.ProductDetail;
import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.PagedResponse;

import reactor.core.publisher.Mono;

public interface CustomerProductService {
  Mono<ProductDetail> getProductDetail(UUID id);
  List<ProductVariant> getProductVariantDetails(List<String> productVariantIds);
  Mono<PagedResponse<ProductVariant>> searchProducts(SearchProductRequest request);
}
