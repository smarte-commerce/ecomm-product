package com.winnguyen1905.product.core.model.viewmodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record PagedResponse<T>(
    int page,
    int size,
    int maxPageItems,
    @JsonProperty("results") List<T> results,
    long totalElements,
    int totalPages)
    implements AbstractModel {
  @Builder
  public PagedResponse(
      int page,
      int size,
      int maxPageItems,
      List<T> results,
      long totalElements,
      int totalPages) {
    this.page = page;
    this.size = size;
    this.maxPageItems = maxPageItems;
    this.results = results;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
  }
}
