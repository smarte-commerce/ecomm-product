package com.winnguyen1905.product.core.model;

import com.fasterxml.jackson.databind.JsonNode;

import io.micrometer.common.lang.NonNull;

public record Variation(
    @NonNull JsonNode detail,
    @NonNull Integer price) implements AbstractModel {
}
