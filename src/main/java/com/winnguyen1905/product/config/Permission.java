package com.winnguyen1905.product.config;

import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record Permission(
    UUID id,
    String name,
    String code,
    String apiPath,
    String method,
    String module)
    implements AbstractModel {
}
