package com.winnguyen1905.product.persistance.repository.custom.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.core.builder.ElasticSearchQueryBuilder;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductESCustomRepositoryImpl implements ProductESCustomRepository {

  private final ElasticsearchOperations elasticsearchOperations;

  @Override
  public Iterable<ESProductVariant> persistAllProductVariants(List<ESProductVariant> esProductVariants) {
    return this.elasticsearchOperations.save(esProductVariants);
  }

  @Override
  public SearchHits<ESProductVariant> searchProducts(SearchProductRequest searchProductRequest, Class<ESProductVariant> clazz) {
    NativeQuery searchQuery = ElasticSearchQueryBuilder.createSearchQuery(searchProductRequest);
    return this.elasticsearchOperations.search(searchQuery, ESProductVariant.class);
  }
}
