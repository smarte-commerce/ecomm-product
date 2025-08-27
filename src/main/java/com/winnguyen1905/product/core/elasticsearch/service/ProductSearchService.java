package com.winnguyen1905.product.core.elasticsearch.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.core.elasticsearch.query.ProductSearchQuery;
import com.winnguyen1905.product.core.elasticsearch.repository.ProductElasticsearchRepository;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.exception.BusinessLogicException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!local")  // Exclude from local profile
@RequiredArgsConstructor
public class ProductSearchService {

  private final ProductElasticsearchRepository productElasticsearchRepository;
  private final ProductSearchQuery productSearchQuery;
  private final ProductDocumentMapper productDocumentMapper;

  @Cacheable(value = "product-search", key = "#searchProductRequest.toString()")
  public PagedResponse<ProductVariantReviewVm> searchProducts(SearchProductRequest searchProductRequest) {
    log.info("Searching products with request: {} (partition-first: {})", 
             searchProductRequest, searchProductRequest.isPartitionFirstEnabled());

    try {
      SearchHits<ProductDocument> searchHits = productSearchQuery.executeSearch(searchProductRequest);

      List<ProductVariantReviewVm> content = searchHits.getSearchHits().stream()
          .map(hit -> productDocumentMapper.toProductVariantReviewVm(hit.getContent()))
          .toList();

      Pageable pageable = PageRequest.of(
          searchProductRequest.pagination().pageNum(),
          searchProductRequest.pagination().pageSize());

      // Add partition information to the response
      PagedResponse.PagedResponseBuilder<ProductVariantReviewVm> responseBuilder = 
          PagedResponse.<ProductVariantReviewVm>builder()
              .content(content)
              .pageNumber(pageable.getPageNumber())
              .pageSize(pageable.getPageSize())
              .totalElements(searchHits.getTotalHits())
              .totalPages((int) Math.ceil((double) searchHits.getTotalHits() / pageable.getPageSize()))
              .isLastPage(
                  pageable.getPageNumber() >= Math.ceil((double) searchHits.getTotalHits() / pageable.getPageSize()) - 1);

      // Add metadata for partition-first search
      if (searchProductRequest.isPartitionFirstEnabled()) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("partitionFirstEnabled", true);
        metadata.put("primaryRegion", searchProductRequest.region() != null ? 
                    searchProductRequest.region().getCode() : null);
        metadata.put("partitionFirstThreshold", searchProductRequest.getPartitionFirstThreshold());
        metadata.put("searchStrategy", searchHits.getTotalHits() < 
                    (searchProductRequest.getPage().getPageSize() * searchProductRequest.getPartitionFirstThreshold()) ? 
                    "multi-partition" : "partition-only");
        
        log.info("Partition-first search completed: {} results, strategy: {}", 
                searchHits.getTotalHits(), metadata.get("searchStrategy"));
      }

      return responseBuilder.build();

    } catch (Exception e) {
      log.error("Error searching products: {}", e.getMessage(), e);
      throw new BusinessLogicException("Failed to search products: " + e.getMessage());
    }
  }

  @Cacheable(value = "product-category-search", key = "#categoryId + '_' + #pageable.toString()")
  public Page<ProductDocument> searchByCategory(String categoryId, Pageable pageable) {
    log.info("Searching products by category: {}", categoryId);

    try {
      return productElasticsearchRepository.findByCategoryId(categoryId, pageable);
    } catch (Exception e) {
      log.error("Error searching products by category: {}", e.getMessage(), e);
      throw new BusinessLogicException("Failed to search products by category: " + e.getMessage());
    }
  }

  @Cacheable(value = "product-price-search", key = "#minPrice + '_' + #maxPrice + '_' + #pageable.toString()")
  public Page<ProductDocument> searchByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
    log.info("Searching products by price range: {} - {}", minPrice, maxPrice);

    try {
      return productElasticsearchRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    } catch (Exception e) {
      log.error("Error searching products by price range: {}", e.getMessage(), e);
      throw new BusinessLogicException("Failed to search products by price range: " + e.getMessage());
    }
  }

  @Cacheable(value = "product-suggestions", key = "#query")
  public List<String> getSearchSuggestions(String query) {
    log.info("Getting search suggestions for query: {}", query);

    try {
      return productElasticsearchRepository.findSuggestions(query);
    } catch (Exception e) {
      log.error("Error getting search suggestions: {}", e.getMessage(), e);
      throw new BusinessLogicException("Failed to get search suggestions: " + e.getMessage());
    }
  }

  @Cacheable(value = "similar-products", key = "#productId")
  public List<ProductDocument> findSimilarProducts(String productId, int limit) {
    log.info("Finding similar products for product ID: {}", productId);

    try {
      return productElasticsearchRepository.findSimilarProducts(productId, PageRequest.of(0, limit))
          .getContent();
    } catch (Exception e) {
      log.error("Error finding similar products: {}", e.getMessage(), e);
      throw new BusinessLogicException("Failed to find similar products: " + e.getMessage());
    }
  }

  @Cacheable(value = "popular-products", key = "#limit")
  public List<ProductDocument> getPopularProducts(int limit) {
    log.info("Getting popular products with limit: {}", limit);

    try {
      return productElasticsearchRepository.findPopularProducts(PageRequest.of(0, limit))
          .getContent();
    } catch (Exception e) {
      log.error("Error getting popular products: {}", e.getMessage(), e);
      throw new BusinessLogicException("Failed to get popular products: " + e.getMessage());
    }
  }

  @Async
  public CompletableFuture<List<ProductDocument>> searchProductsAsync(SearchProductRequest searchProductRequest) {
    log.info("Async searching products with request: {}", searchProductRequest);

    try {
      SearchHits<ProductDocument> searchHits = productSearchQuery.executeSearch(searchProductRequest);
      List<ProductDocument> products = searchHits.getSearchHits().stream()
          .map(hit -> hit.getContent())
          .toList();

      return CompletableFuture.completedFuture(products);
    } catch (Exception e) {
      log.error("Error in async product search: {}", e.getMessage(), e);
      CompletableFuture<List<ProductDocument>> future = new CompletableFuture<>();
      future.completeExceptionally(new BusinessLogicException("Failed to search products async: " + e.getMessage()));
      return future;
    }
  }
}
