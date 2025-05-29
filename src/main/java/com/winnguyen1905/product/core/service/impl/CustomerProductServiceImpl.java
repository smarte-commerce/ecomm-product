package com.winnguyen1905.product.core.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.mapper_v2.ProductESMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductMapper;
import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantByShopVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.core.service.CustomerProductService;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;
import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProductServiceImpl implements CustomerProductService {

  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final ProductESCustomRepository productESRepository;

  @Override
  public ProductDetailVm getProductDetail(UUID id) {
    return productRepository.findByIdAndIsPublishedTrue(id)
        .map(ProductMapper::toProductDetail)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
  }

  @Override
  public PagedResponse<ProductVariantReviewVm> searchProducts(SearchProductRequest searchProductRequest) {
    SearchHits<ESProductVariant> searchHits = productESRepository.searchProducts(searchProductRequest,
        ESProductVariant.class);

    List<ESProductVariant> esProductVariants = searchHits.getSearchHits()
        .stream()
        .map(SearchHit::getContent)
        .toList();

    log.info("Number of products found: {}", esProductVariants.size());

    List<ProductVariantReviewVm> productVariantResponses = ProductESMapper
        .toProductVariantReviews(esProductVariants);

    log.info("Number of product variant reviews mapped: {}", productVariantResponses.size());

    return PagedResponse.<ProductVariantReviewVm>builder()
        .results(productVariantResponses)
        .page(searchProductRequest.getPage().pageNum())
        .size(searchProductRequest.getPage().pageSize())
        .totalElements((int) searchHits.getTotalHits())
        .build();
  }

  @Override
  public ProductVariantByShopVm getProductVariantDetails(List<String> productVariantIds) {
    List<ProductVariantByShopVm.ShopProductVariant> cartByShopProductItems = this.productESRepository
        .findByIds(productVariantIds).stream()
        .collect(Collectors.groupingBy(ESProductVariant::getShopId))
        .entrySet().stream()
        .map(entry -> new ProductVariantByShopVm.ShopProductVariant(
            entry.getKey(),
            entry.getValue().stream()
                .map(ProductMapper::toProductVariantReview)
                .collect(Collectors.toList())))
        .collect(Collectors.toList());

    return new ProductVariantByShopVm(cartByShopProductItems);
  }

  @Override
  public List<ProductVariantDetailVm> getProductVariantDetails(UUID productId) {
    List<ProductVariantDetailVm> productVariants = this.productVariantRepository
        .findVariantsByProductId(productId).stream()
        .map(productVariant -> ProductVariantDetailVm.builder()
            .id(productVariant.getId())
            .sku(productVariant.getSku())
            .price(productVariant.getPrice())
            .build())
        .collect(Collectors.toList());
    return productVariants;
  }

}
