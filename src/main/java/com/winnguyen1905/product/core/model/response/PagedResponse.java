package com.winnguyen1905.product.core.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnguyen1905.product.core.model.AbstractModel; 

public record PagedResponse<T>(Integer maxPageItems,
    Integer page,
    Integer size,
    @JsonProperty("results") List<T> results,
    Integer totalElements,
    Integer totalPages)
    implements AbstractModel {
}
