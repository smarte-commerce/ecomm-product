package com.winnguyen1905.product.core.model.response;

import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record Category(
    UUID id,
    UUID parentId,
    String name,
    Integer left,
    Integer right,
    UUID shopId,
    boolean isPublished,
    String description) implements AbstractModel {
}
