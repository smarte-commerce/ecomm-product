package com.winnguyen1905.product.core.service.impl;

import java.util.List;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.mapper.ProductESMapper;
import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.core.service.ElasticSearchService;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EProduct;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticSearchServiceImpl implements ElasticSearchService {

  private final ProductESMapper productESMapper;
  private final ElasticsearchOperations elasticsearchOperations;

  @Override
  public Flux<Void> persistProductVariants(EProduct product) {
    List<ESProductVariant> variants = this.productESMapper.toESProductVariants(product);
    log.info("Saving {} product variants to Elasticsearch", variants.size());

    return Flux.fromIterable(variants)
        .subscribeOn(Schedulers.parallel())
        .publishOn(Schedulers.boundedElastic())
        .map(esProductVariant -> {
          ESProductVariant saved = elasticsearchOperations.save(esProductVariant);
          log.debug("Saved product variant with ID: {}", saved.getId());
          return saved;
        })
        .flatMap(__ -> Mono.empty());
  }

  @Override
  public Mono<PagedResponse<ProductVariant>> searchProducts(SearchRequest request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'searchProducts'");
  }

}
