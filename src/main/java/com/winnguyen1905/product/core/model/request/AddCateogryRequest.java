package com.winnguyen1905.product.core.model.request;

public record AddCateogryRequest(
    String name,
    String description,
    Integer left,
    Integer right,
    Boolean isPublished,
    String parentId,
    String shopId
) {}
