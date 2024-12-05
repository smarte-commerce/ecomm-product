package com.winnguyen1905.product.core.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record Inventory(
    UUID id,
    String createdDate,
    String updatedDate,
    Boolean isDeleted, int stock) implements AbstractModel {
}
