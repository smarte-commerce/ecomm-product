package com.winnguyen1905.product.core.builder;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.util.StringUtils;

import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.util.CommonUtils;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchQueryBuilder {

  public static NativeQuery createSearchQuery(SearchProductRequest searchProductRequest) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    setFilters(searchProductRequest, boolQueryBuilder);

    setMatchQuery(searchProductRequest, boolQueryBuilder);

    Query finalQuery = Query.of(bool -> bool.bool(boolQueryBuilder.build()));

    List<SortOptions> sortOptions = getSortOptions(searchProductRequest);

    NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder().withQuery(finalQuery);

    if (sortOptions != null && !sortOptions.isEmpty()) {
      nativeQueryBuilder.withSort(sortOptions);
    }

    NativeQuery nativeQuery = nativeQueryBuilder.build();

    setPagination(searchProductRequest, nativeQuery);

    return nativeQuery;
  }

  private static void setPagination(SearchProductRequest searchProductRequest, NativeQuery nativeQuery) {
    if (Objects.nonNull(searchProductRequest.pagination())) {
      nativeQuery.setPageable(
          Pageable.ofSize(searchProductRequest.pagination().pageSize())
              .withPage(searchProductRequest.pagination().pageNum()));
    }
  }

  private static List<SortOptions> getSortOptions(SearchProductRequest searchProductRequest) {
    return CommonUtils.stream(searchProductRequest.sorts())
        .map(sort -> new SortOptions.Builder()
            .field(f -> f
                .field(sort.field())
                .order("asc".equalsIgnoreCase(sort.order()) ? SortOrder.Asc : SortOrder.Desc))
            .build())
        .toList();
  }

  private static void setMatchQuery(SearchProductRequest searchProductRequest, BoolQuery.Builder bq) {
    if (StringUtils.hasLength(searchProductRequest.searchTerm())) {
      bq.must(
          m -> m.match(
              matchQuery -> matchQuery.query(q -> q.stringValue(searchProductRequest.searchTerm())).field("name")));
    }
  }

  private static void setFilters(SearchProductRequest searchProductRequest, BoolQuery.Builder bq) {
    CommonUtils.stream(searchProductRequest.filters())
        .forEach(
            filter -> {
              TermsQueryField valueTerms = new TermsQueryField.Builder()
                  .value(
                      filter.values().stream()
                          .map(String::toLowerCase)
                          .map(FieldValue::of)
                          .toList())
                  .build();

              TermsQuery termsQuery = TermsQuery.of(t -> t.field(filter.field()).terms(valueTerms));
              bq.filter(f -> f.terms(termsQuery));
            });
  }
}
