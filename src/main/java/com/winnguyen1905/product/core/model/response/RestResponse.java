package com.winnguyen1905.product.core.model.response;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record RestResponse<T>(
    Integer statusCode,
    String error,
    Object message,
    T data)
    implements AbstractModel {
}
