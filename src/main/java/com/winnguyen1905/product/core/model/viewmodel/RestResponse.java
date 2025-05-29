package com.winnguyen1905.product.core.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
@JsonInclude(value = Include.NON_NULL)
public record RestResponse<T>(
    Integer statusCode,
    String error,
    Object message,
    T data)
    implements AbstractModel {
  @Builder
  public RestResponse(Integer statusCode, String error, Object message, T data) {
    this.statusCode = statusCode;
    this.error = error;
    this.message = message;
    this.data = data;
  }
}
