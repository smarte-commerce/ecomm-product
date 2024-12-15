package com.winnguyen1905.product.core.service.impl;

import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.core.service.ElasticSearchService;
import com.winnguyen1905.product.persistance.entity.EProduct;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import reactor.core.publisher.Mono;

public class ElasticSearchServiceImpl implements ElasticSearchService {

  @Override
  public Mono<Void> persistProduct(EProduct product) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'persistProduct'");
  }

  @Override
  public Mono<PagedResponse<ProductVariant>> searchProducts(SearchRequest request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'searchProducts'");
  }
  
}
