package com.winnguyen1905.product.core.service.customer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.winnguyen1905.product.core.mapper.ProductESMapper;
import com.winnguyen1905.product.core.mapper.ProductMapper;
import com.winnguyen1905.product.core.model.ProductDetail;
import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant; 
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;
import com.winnguyen1905.product.util.CommonUtils;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProductServiceImpl implements CustomerProductService {

  private final ProductMapper productMapper;
  private final ProductESMapper productESMapper;
  private final ProductRepository productRepository;
  private final ProductESCustomRepository productESRepository;

  @Override
  public Mono<ProductDetail> getProductDetail(UUID id) {
    return Mono.fromCallable(() -> this.productRepository.findByIdAndIsPublishedTrue(id))
        .flatMap(CommonUtils::toMono)
        .switchIfEmpty(Mono.error(new EntityNotFoundException("Product not found with id: " + id)))
        .map(productMapper::toProductDto);
  }

  @Override
  public Mono<PagedResponse<ProductVariant>> searchProducts(SearchProductRequest request) {
    return Mono.fromCallable(() -> this.productESRepository.searchProducts(request, ESProductVariant.class))
        .subscribeOn(Schedulers.boundedElastic())
        .map(searchHits -> {
          List<ESProductVariant> esProductVariants = searchHits.getSearchHits()
              .stream().map(SearchHit::getContent).toList();
          List<ProductVariant> products = this.productESMapper.with(esProductVariants);

          return PagedResponse.<ProductVariant>builder()
              .page(request.getPage().pageNum())
              .size(request.getPage().pageSize())
              .results(products)
              .totalElements((int) searchHits.getTotalHits())
              .build();
        });
  }

  @Override
  public List<ProductVariant> getProductVariantDetails(List<String> productVariantIds) {
    return this.productESRepository.findByIds(productVariantIds).stream()
        .map(productESMapper::with)
        .toList();
  }

}
