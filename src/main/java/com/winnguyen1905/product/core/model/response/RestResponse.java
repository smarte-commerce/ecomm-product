package com.winnguyen1905.product.core.model.response;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.*;

@Getter
@Setter
@Builder
public class RestResponse<T> extends AbstractModel {
    private Integer statusCode;
    private String error;
    private Object message;
    private T data;
}
