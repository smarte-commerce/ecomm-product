package com.winnguyen1905.product.core.service;

import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.persistance.entity.EProduct;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ElasticSearchService {
  Flux<Void> persistProductVariants(EProduct product);
  Mono<PagedResponse<ProductVariant>> searchProducts(SearchRequest request);
}
