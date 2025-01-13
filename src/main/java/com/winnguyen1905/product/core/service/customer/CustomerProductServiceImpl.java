package com.winnguyen1905.product.core.service.customer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.winnguyen1905.product.core.mapper.ProductVariantMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductESMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductMapper;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.CartByShopProductResponse;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.core.model.response.ProductDetail;
import com.winnguyen1905.product.core.model.response.ProductVariantReview;
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

  private final ProductRepository productRepository;
  private final ProductESCustomRepository productESRepository;

  @Override
  public Mono<ProductDetail> getProductDetail(UUID id) {
    return Mono.fromCallable(() -> this.productRepository.findByIdAndIsPublishedTrue(id))
        .flatMap(CommonUtils::toMono)
        .switchIfEmpty(Mono.error(new EntityNotFoundException("Product not found with id: " + id)))
        .map(ProductMapper::toProductDetail);
  }

  @Override
  public Mono<PagedResponse<ProductVariantReview>> searchProducts(SearchProductRequest request) {
    return Mono.fromCallable(() -> this.productESRepository.searchProducts(request, ESProductVariant.class))
        .subscribeOn(Schedulers.boundedElastic())
        .map(searchHits -> {
          List<ESProductVariant> esProductVariants = searchHits.getSearchHits()
              .stream().map(SearchHit::getContent).toList();
          List<ProductVariantReview> productVariantResponses = ProductESMapper
              .toProductVariantReviews(esProductVariants);
          return PagedResponse.<ProductVariantReview>builder()
              .page(request.getPage().pageNum())
              .size(request.getPage().pageSize())
              .results(productVariantResponses)
              .totalElements((int) searchHits.getTotalHits())
              .build();
        });
  }

  @Override
public CartByShopProductResponse getProductVariantDetails(List<String> productVariantIds) {
    List<CartByShopProductResponse.CartByShopProductItem> cartByShopProductItems = 
        this.productESRepository.findByIds(productVariantIds).stream()
            .collect(Collectors.groupingBy(ESProductVariant::getShopId)) 
            .entrySet().stream()
            .map(entry -> new CartByShopProductResponse.CartByShopProductItem(
                entry.getKey(),  
                entry.getValue().stream()
                    .map(ProductMapper::toProductVariantReview)  
                    .collect(Collectors.toList())))  
            .collect(Collectors.toList());  

    return new CartByShopProductResponse(cartByShopProductItems);
}


}
