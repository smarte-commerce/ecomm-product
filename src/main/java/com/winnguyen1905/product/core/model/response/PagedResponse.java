package com.winnguyen1905.product.core.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder; 

@Builder
public record PagedResponse<T>(
    int page,
    int size,
    int maxPageItems,
    @JsonProperty("results") List<T> results,
    int totalElements,
    int totalPages)
    implements AbstractModel {
}
