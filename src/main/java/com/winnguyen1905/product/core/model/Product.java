package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record Product(
    UUID id,
    String name,
    String slug,
    String brand,
    String thumb,
    Double price,
    String productType,
    String description,
    List<Variation> variations,
    Boolean isDeleted,
    @JsonFormat(pattern = "HH-mm-ss a dd-MM-yyyy", timezone = "GMT+7") String createdDate,
    @JsonFormat(pattern = "HH-mm-ss a dd-MM-yyyy", timezone = "GMT+7") String updatedDate) implements AbstractModel {
}
