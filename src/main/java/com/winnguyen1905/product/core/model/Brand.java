package com.winnguyen1905.product.core.model;

import java.util.UUID;

public record Brand(
    UUID id,
    String createdDate,
    String updatedDate,
    String name, String description) {
}
