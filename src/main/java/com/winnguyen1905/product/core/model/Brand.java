package com.winnguyen1905.product.core.model;

import java.util.UUID;

public record Brand(
    UUID id,
    String name,
    String description,
    boolean isVerified,
    String createdDate,
    String updatedDate) implements AbstractModel {
}
