package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.Objects;

import org.springframework.integration.annotation.Default;

import com.winnguyen1905.product.secure.RegionPartition;

public record SearchProductRequest(
    List<Sort> sorts,
    String searchTerm,
    List<Filter> filters,
    Pagination pagination,
    RegionPartition region) implements AbstractModel {

  public Pagination getPage() {
    if (Objects.isNull(pagination)) {
      return new SearchProductRequest.Pagination(0, 0);
    }
    return pagination;
  }

  public static record Pagination(
      int pageSize,
      int pageNum) implements AbstractModel {
    public Pagination {
      pageSize = pageSize == 0 ? 15 : pageSize;
      pageNum = pageNum == 0 ? 0 : pageNum;
    }
  }

  public static record Filter(
      String field,
      List<String> values) implements AbstractModel {
  }

  public static record Sort(
      String order,
      String field) implements AbstractModel {
  }

}
