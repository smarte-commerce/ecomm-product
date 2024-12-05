package com.winnguyen1905.product.core.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record Inventory(
    UUID id,
    @JsonFormat(pattern = "HH-mm-ss a dd-MM-yyyy", timezone = "GMT+7") String createdDate,
    @JsonFormat(pattern = "HH-mm-ss a dd-MM-yyyy", timezone = "GMT+7") String updatedDate,
    String createdBy,
    String updatedBy,
    Boolean isDeleted, int stock) implements AbstractModel {}
