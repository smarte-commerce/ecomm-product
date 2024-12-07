package com.winnguyen1905.product.core.builder;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.util.CommonUtils;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchQueryBuilder {

  public static NativeQuery createSearchQuery(SearchProductRequest searchProductRequest) {

    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    setFilters(searchProductRequest, boolQueryBuilder);

    setMatchQuery(searchProductRequest, boolQueryBuilder);

    Query finalQuery = Query.of(bool -> bool.bool(boolQueryBuilder.build()));

    SortOptions sortOptions = getSortOptions(searchProductRequest);

    NativeQuery nativeQuery = NativeQuery.builder().withQuery(finalQuery).withSort(List.of(sortOptions)).build();

    setPagination(searchProductRequest, nativeQuery);

    return nativeQuery;
  }

  private static void setPagination(SearchProductRequest searchProductRequest, NativeQuery nativeQuery) {
    if (Objects.nonNull(searchProductRequest.pagination())) {
      nativeQuery.setPageable(
          Pageable.ofSize(searchProductRequest.pagination().getPageSize())
              .withPage(searchProductRequest.pagination().getPageNo()));
    }
  }

  private static SortOptions getSortOptions(SearchProductRequest searchProductRequest) {
    SortOptions.Builder sortOptionsBuilder = new SortOptions.Builder();

    CommonUtils.stream(searchProductRequest.getSorts())
        .forEach(
            sort -> sortOptionsBuilder.field(
                f -> f.field(sort.getField())
                    .order(
                        sort.getOrder().equals("asc") ? SortOrder.Asc : SortOrder.Desc)));

    return sortOptionsBuilder.build();
  }

  private static void setMatchQuery(SearchProductRequest searchProductRequest, BoolQuery.Builder bq) {
    if (StringUtils.hasLength(searchProductRequest.getSearchTerm())) {
      bq.must(
          m -> m.match(
              matchQuery -> matchQuery.query(q -> q.stringValue(searchProductRequest.getSearchTerm())).field("name")));
    }
  }

  private static void setFilters(SearchProductRequest searchProductRequest, BoolQuery.Builder bq) {
    CommonUtils.stream(searchProductRequest.getFilters())
        .forEach(
            filter -> {
              TermsQueryField valueTerms = new TermsQueryField.Builder()
                  .value(
                      filter.getValues().stream()
                          .map(String::toLowerCase)
                          .map(FieldValue::of)
                          .toList())
                  .build();

              TermsQuery termsQuery = TermsQuery.of(t -> t.field(filter.getField()).terms(valueTerms));
              bq.filter(f -> f.terms(termsQuery));
            });
  }

  private static Query multipleTermsQuery(TermsQueryField terms) {
    return Query.of(
        q -> q.bool(
            b -> b.filter(f -> f.terms(t -> t.field("features.Brand").terms(terms)))
                .filter(f -> f.terms(t -> t.field("category.name").terms(terms)))));
  }

  private static Query getSingleTerm(JsonData data) {
    return Query.of(
        q -> q.bool(
            b -> b.filter(
                f -> f.term(t -> t.field("features.Brand").value(v -> v.anyValue(data))))));
  }
}
