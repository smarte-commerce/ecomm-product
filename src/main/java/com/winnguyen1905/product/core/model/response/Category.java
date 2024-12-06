package com.winnguyen1905.product.core.model.response;

import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

public record Category(
    UUID parentId,

    UUID id,

    String name,

    Integer left,

    Integer right,

    UUID shopId,

    String description) implements AbstractModel {
}
