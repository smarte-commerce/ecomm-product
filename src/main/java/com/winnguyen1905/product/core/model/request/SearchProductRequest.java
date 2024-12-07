package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.Objects;

import org.springframework.integration.annotation.Default;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchProductRequest(
    List<Sort> sorts,
    String searchTerm,
    List<Filter> filters,
    Pagination pagination) {
  public Pagination getPage() {
    if (Objects.isNull(pagination)) {
      return new SearchProductRequest.Pagination(0, 0);
    }
    return pagination;
  }

  public static record Pagination(
      @Default int pageSize,
      @Default int pageNo) {
    public Pagination {
      pageSize = pageSize == 0 ? Integer.MAX_VALUE : pageSize;
      pageNo = pageNo == 0 ? 0 : pageNo;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static record Filter(
      String field,
      List<String> values) {
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static record Sort(
      String order,
      String field) {
  }

}
