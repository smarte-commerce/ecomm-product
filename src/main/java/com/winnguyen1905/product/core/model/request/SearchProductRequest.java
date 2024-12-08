package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.Objects;

import org.springframework.integration.annotation.Default;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.winnguyen1905.product.core.model.AbstractModel;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchProductRequest(
    List<Sort> sorts,
    String searchTerm,
    List<Filter> filters,
    Pagination pagination) implements AbstractModel {
  public Pagination getPage() {
    if (Objects.isNull(pagination)) {
      return new SearchProductRequest.Pagination(0, 0);
    }
    return pagination;
  }

  public static record Pagination(
      @Default int pageSize,
      @Default int pageNum) {
    public Pagination {
      pageSize = pageSize == 0 ? Integer.MAX_VALUE : pageSize;
      pageNum = pageNum == 0 ? 0 : pageNum;
    }
  }

  public static record Filter(
      String field,
      List<String> values) {
  }

  public static record Sort(
      String order,
      String field) {
  }

}
