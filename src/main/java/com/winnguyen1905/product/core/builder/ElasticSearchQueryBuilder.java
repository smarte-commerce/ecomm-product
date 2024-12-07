package com.winnguyen1905.product.core.builder;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchQueryBuilder {

  public static NativeQuery createSearchQuery(SearchRequest request) {

    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    setMatchQuery(request, boolQueryBuilder);
    setFilters(request, boolQueryBuilder);

    Query finalQuery = Query.of(bool -> bool.bool(boolQueryBuilder.build()));

    SortOptions sortOptions = getSortOptions(request);

    NativeQuery nativeQuery =
        NativeQuery.builder().withQuery(finalQuery).withSort(List.of(sortOptions)).build();

    setPagination(request, nativeQuery);

    log.info("Elasticsearch Product search query ::: {}", nativeQuery.getQuery());

    return nativeQuery;
  }

  private static void setPagination(SearchRequest request, NativeQuery nativeQuery) {
    if (Objects.nonNull(request.getPagination())) {
      nativeQuery.setPageable(
          Pageable.ofSize(request.getPagination().getPageSize())
              .withPage(request.getPagination().getPageNo()));
    }
  }

  private static SortOptions getSortOptions(SearchRequest request) {
    SortOptions.Builder sortOptionsBuilder = new SortOptions.Builder();

    Utility.stream(request.getSorts())
        .forEach(
            sort ->
                sortOptionsBuilder.field(
                    f ->
                        f.field(sort.getField())
                            .order(
                                sort.getOrder().equals("asc") ? SortOrder.Asc : SortOrder.Desc)));

    return sortOptionsBuilder.build();
  }

  private static void setMatchQuery(SearchRequest request, BoolQuery.Builder bq) {
    if (StringUtils.hasLength(request.getSearchTerm())) {
      bq.must(
          m ->
              m.match(
                  matchQuery ->
                      matchQuery.query(q -> q.stringValue(request.getSearchTerm())).field("name")));
    }
  }

  private static void setFilters(SearchRequest request, BoolQuery.Builder bq) {
    Utility.stream(request.getFilters())
        .forEach(
            filter -> {
              TermsQueryField valueTerms =
                  new TermsQueryField.Builder()
                      .value(
                          filter.getValues().stream()
                              .map(String::toLowerCase)
                              .map(FieldValue::of)
                              .toList())
                      .build();

              TermsQuery termsQuery =
                  TermsQuery.of(t -> t.field(filter.getField()).terms(valueTerms));
              bq.filter(f -> f.terms(termsQuery));
            });
  }

  private static Query multipleTermsQuery(TermsQueryField terms) {
    return Query.of(
        q ->
            q.bool(
                b ->
                    b.filter(f -> f.terms(t -> t.field("features.Brand").terms(terms)))
                        .filter(f -> f.terms(t -> t.field("category.name").terms(terms)))));
  }

  private static Query getSingleTerm(JsonData data) {
    return Query.of(
        q ->
            q.bool(
                b ->
                    b.filter(
                        f -> f.term(t -> t.field("features.Brand").value(v -> v.anyValue(data))))));
  }
}
