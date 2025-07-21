package com.winnguyen1905.product.core.elasticsearch.query;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.secure.RegionPartition;
import com.winnguyen1905.product.util.CommonUtils;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchQuery {

  private final ElasticsearchOperations elasticsearchOperations;

  public SearchHits<ProductDocument> executeSearch(SearchProductRequest searchProductRequest) {
    if (searchProductRequest.isPartitionFirstEnabled() && searchProductRequest.region() != null) {
      return executePartitionFirstSearch(searchProductRequest);
    }

    NativeQuery searchQuery = buildSearchQuery(searchProductRequest);
    return elasticsearchOperations.search(searchQuery, ProductDocument.class);
  }

  /**
   * Execute partition-first search: search user's partition first, then expand if
   * needed
   */
  public SearchHits<ProductDocument> executePartitionFirstSearch(SearchProductRequest searchProductRequest) {
    // Step 1: Search in user's partition only
    SearchHits<ProductDocument> primaryResults = searchInSpecificPartition(searchProductRequest,
        searchProductRequest.region());

    int desiredResults = searchProductRequest.getPage().getPageSize();
    int thresholdResults = (int) (desiredResults * searchProductRequest.getPartitionFirstThreshold());

    log.debug("Partition-first search: found {} results in partition {}, threshold: {}",
        primaryResults.getTotalHits(), searchProductRequest.region().getCode(), thresholdResults);

    // Step 2: If insufficient results, search other partitions
    if (primaryResults.getTotalHits() < thresholdResults) {
      return executeMultiPartitionSearch(searchProductRequest, primaryResults);
    }

    return primaryResults;
  }

  /**
   * Search in a specific partition
   */
  public SearchHits<ProductDocument> searchInSpecificPartition(SearchProductRequest searchProductRequest,
      RegionPartition region) {
    NativeQuery searchQuery = buildPartitionSpecificQuery(searchProductRequest, region);
    return elasticsearchOperations.search(searchQuery, ProductDocument.class);
  }

  /**
   * Execute multi-partition search combining results from primary partition and
   * others
   */
  private SearchHits<ProductDocument> executeMultiPartitionSearch(SearchProductRequest searchProductRequest,
      SearchHits<ProductDocument> primaryResults) {
    // Get results from other partitions
    SearchHits<ProductDocument> otherPartitionResults = searchInOtherPartitions(searchProductRequest);

    // Combine results with proper boosting
    return combinePartitionResults(primaryResults, otherPartitionResults, searchProductRequest);
  }

  /**
   * Search in all partitions except the user's primary partition
   */
  private SearchHits<ProductDocument> searchInOtherPartitions(SearchProductRequest searchProductRequest) {
    // Create a copy of request for searching other partitions
    SearchProductRequest otherPartitionRequest = SearchProductRequest.builder()
        .sorts(searchProductRequest.sorts())
        .searchTerm(searchProductRequest.searchTerm())
        .keyword(searchProductRequest.keyword())
        .filters(searchProductRequest.filters())
        .pagination(SearchProductRequest.Pagination.builder()
            .pageNum(0) // Always start from page 0 for other partitions
            .pageSize(searchProductRequest.getMaxResultsFromOtherPartitions())
            .build())
        .region(null) // No region restriction for other partitions
        .productType(searchProductRequest.productType())
        .status(searchProductRequest.status())
        .isPublished(searchProductRequest.isPublished())
        .enablePartitionFirst(false) // Disable to avoid recursion
        .build();

    NativeQuery searchQuery = buildMultiPartitionQuery(otherPartitionRequest, searchProductRequest.region());
    return elasticsearchOperations.search(searchQuery, ProductDocument.class);
  }

  public NativeQuery buildSearchQuery(SearchProductRequest searchProductRequest) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Add text search
    addTextSearch(searchProductRequest, boolQueryBuilder);

    // Add filters
    addFilters(searchProductRequest, boolQueryBuilder);

    // Add status filters
    addStatusFilters(boolQueryBuilder);

    Query baseQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    // Apply region boosting
    Query finalQuery = applyRegionBoosting(searchProductRequest, baseQuery);

    // Build native query with sorting and pagination
    NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
        .withQuery(finalQuery);

    // Add sorting
    addSorting(searchProductRequest, nativeQueryBuilder);

    // Add pagination
    addPagination(searchProductRequest, nativeQueryBuilder);

    return nativeQueryBuilder.build();
  }

  public NativeQuery buildSimilarProductsQuery(String productId, int limit) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Exclude the current product
    boolQueryBuilder.mustNot(TermQuery.of(t -> t.field("id").value(productId))._toQuery());

    // Add status filters
    addStatusFilters(boolQueryBuilder);

    // More like this query would be implemented here
    // For now, using a simple approach
    Query baseQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    return NativeQuery.builder()
        .withQuery(baseQuery)
        .withPageable(Pageable.ofSize(limit))
        .build();
  }

  public NativeQuery buildCategoryQuery(String categoryId, Pageable pageable) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Category filter
    boolQueryBuilder.filter(TermQuery.of(t -> t.field("category.id").value(categoryId))._toQuery());

    // Add status filters
    addStatusFilters(boolQueryBuilder);

    Query baseQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    return NativeQuery.builder()
        .withQuery(baseQuery)
        .withPageable(pageable)
        .build();
  }

  public NativeQuery buildPriceRangeQuery(Double minPrice, Double maxPrice, Pageable pageable) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Price range filter
    RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field("price");
    if (minPrice != null) {
      rangeBuilder.gte(JsonData.of(minPrice));
    }
    if (maxPrice != null) {
      rangeBuilder.lte(JsonData.of(maxPrice));
    }

    boolQueryBuilder.filter(rangeBuilder.build()._toQuery());

    // Add status filters
    addStatusFilters(boolQueryBuilder);

    Query baseQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    return NativeQuery.builder()
        .withQuery(baseQuery)
        .withPageable(pageable)
        .build();
  }

  public NativeQuery buildPopularProductsQuery(Pageable pageable) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Add status filters
    addStatusFilters(boolQueryBuilder);

    Query baseQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    return NativeQuery.builder()
        .withQuery(baseQuery)
        .withSort(SortOptions.of(s -> s.field(f -> f.field("purchase_count").order(SortOrder.Desc))))
        .withSort(SortOptions.of(s -> s.field(f -> f.field("rating").order(SortOrder.Desc))))
        .withPageable(pageable)
        .build();
  }

  private void addTextSearch(SearchProductRequest searchProductRequest, BoolQuery.Builder boolQueryBuilder) {
    if (StringUtils.hasLength(searchProductRequest.getKeyword())) {
      // Multi-match query for better text search
      MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
          .query(searchProductRequest.getKeyword())
          .fields("name^3", "description^2", "brand.name^2", "category.name", "tags", "seo_keywords")
          .fuzziness("AUTO")
          .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And));

      boolQueryBuilder.must(multiMatchQuery._toQuery());
    }
  }

  private void addFilters(SearchProductRequest searchProductRequest, BoolQuery.Builder boolQueryBuilder) {
    if (searchProductRequest.filters() != null) {
      CommonUtils.stream(searchProductRequest.filters())
          .forEach(filter -> {
            TermsQueryField valueTerms = new TermsQueryField.Builder()
                .value(filter.values().stream()
                    .map(String::toLowerCase)
                    .map(FieldValue::of)
                    .toList())
                .build();

            TermsQuery termsQuery = TermsQuery.of(t -> t.field(filter.field()).terms(valueTerms));
            boolQueryBuilder.filter(termsQuery._toQuery());
          });
    }
  }

  private void addStatusFilters(BoolQuery.Builder boolQueryBuilder) {
    // Only show published products
    boolQueryBuilder.filter(TermQuery.of(t -> t.field("is_published").value(true))._toQuery());

    // Only show products with available inventory
    boolQueryBuilder.filter(TermQuery.of(t -> t.field("inventory.is_in_stock").value(true))._toQuery());
  }

  private Query applyRegionBoosting(SearchProductRequest searchProductRequest, Query baseQuery) {
    if (searchProductRequest.region() != null) {
      return Query.of(q -> q.functionScore(fs -> fs
          .query(baseQuery)
          .functions(f -> f
              .filter(Query.of(
                  fq -> fq.term(t -> t.field("region").value(FieldValue.of(searchProductRequest.region().toString())))))
              .weight(5.0))
          .boostMode(FunctionBoostMode.Multiply)
          .scoreMode(FunctionScoreMode.Sum)));
    } else {
      log.warn("SearchProductRequest region is null, not boosting by region.");
      return baseQuery;
    }
  }

  private void addSorting(SearchProductRequest searchProductRequest, NativeQueryBuilder nativeQueryBuilder) {
    if (searchProductRequest.sorts() != null) {
      List<SortOptions> sortOptions = CommonUtils.stream(searchProductRequest.sorts())
          .map(sort -> SortOptions.of(s -> s.field(f -> f
              .field(sort.field())
              .order("asc".equalsIgnoreCase(sort.order()) ? SortOrder.Asc : SortOrder.Desc))))
          .toList();

      if (!sortOptions.isEmpty()) {
        nativeQueryBuilder.withSort(sortOptions);
      }
    }
  }

  private void addPagination(SearchProductRequest searchProductRequest, NativeQueryBuilder nativeQueryBuilder) {
    if (Objects.nonNull(searchProductRequest.pagination())) {
      nativeQueryBuilder.withPageable(
          Pageable.ofSize(searchProductRequest.pagination().pageSize())
              .withPage(searchProductRequest.pagination().pageNum()));
    }
  }

  /**
   * Build query for a specific partition
   */
  private NativeQuery buildPartitionSpecificQuery(SearchProductRequest searchProductRequest, RegionPartition region) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Add text search
    addTextSearch(searchProductRequest, boolQueryBuilder);

    // Add filters
    addFilters(searchProductRequest, boolQueryBuilder);

    // Add region filter
    boolQueryBuilder.filter(TermQuery.of(t -> t.field("region").value(region.getCode()))._toQuery());

    // Add status filters
    addStatusFilters(boolQueryBuilder);

    Query baseQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    // Build native query with sorting and pagination
    NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
        .withQuery(baseQuery);

    // Add sorting
    addSorting(searchProductRequest, nativeQueryBuilder);

    // Add pagination
    addPagination(searchProductRequest, nativeQueryBuilder);

    return nativeQueryBuilder.build();
  }

  /**
   * Build query for all partitions except the specified one
   */
  private NativeQuery buildMultiPartitionQuery(SearchProductRequest searchProductRequest,
      RegionPartition excludeRegion) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Add text search
    addTextSearch(searchProductRequest, boolQueryBuilder);

    // Add filters
    addFilters(searchProductRequest, boolQueryBuilder);

    // Exclude the user's primary partition
    boolQueryBuilder.mustNot(TermQuery.of(t -> t.field("region").value(excludeRegion.getCode()))._toQuery());

    // Add status filters
    addStatusFilters(boolQueryBuilder);

    Query baseQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    // Apply boosting for better results from other regions
    Query boostedQuery = Query.of(q -> q.functionScore(fs -> fs
        .query(baseQuery)
        .functions(f -> f
            .filter(Query.of(fq -> fq.matchAll(ma -> ma)))
            .weight(0.7)) // Slightly lower weight for other partitions
        .boostMode(FunctionBoostMode.Multiply)
        .scoreMode(FunctionScoreMode.Sum)));

    // Build native query with sorting and pagination
    NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
        .withQuery(boostedQuery);

    // Add sorting
    addSorting(searchProductRequest, nativeQueryBuilder);

    // Add pagination
    addPagination(searchProductRequest, nativeQueryBuilder);

    return nativeQueryBuilder.build();
  }

  /**
   * Combine results from different partitions prioritizing the primary partition
   * TODO: Implement proper SearchHits combining when constructor is available
   */
  private SearchHits<ProductDocument> combinePartitionResults(
      SearchHits<ProductDocument> primaryResults,
      SearchHits<ProductDocument> otherResults,
      SearchProductRequest searchProductRequest) {

    log.info("Partition-first search: {} results from primary partition {}, {} from other partitions",
        primaryResults.getTotalHits(), searchProductRequest.region().getCode(), otherResults.getTotalHits());

    // For now, return primary results with logging
    // TODO: Implement proper result combination when SearchHitsImpl constructor is
    // available
    return primaryResults;
  }
}
