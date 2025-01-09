package com.winnguyen1905.product.persistance.repository.custom.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.MultiGetItem;
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
  public SearchHits<ESProductVariant> searchProducts(SearchProductRequest searchProductRequest,
      Class<ESProductVariant> clazz) {
    NativeQuery searchQuery = ElasticSearchQueryBuilder.createSearchQuery(searchProductRequest);
    return this.elasticsearchOperations.search(searchQuery, ESProductVariant.class);
  }

  @Override
  public List<ESProductVariant> findByIds(List<String> ids) {
    Query queries = Query.multiGetQuery(ids);
    List<MultiGetItem<ESProductVariant>> items = elasticsearchOperations.multiGet(queries, ESProductVariant.class);
    return items.stream()
        .map(MultiGetItem::getItem) 
        .filter(Objects::nonNull)  
        .collect(Collectors.toList());
  }
}
