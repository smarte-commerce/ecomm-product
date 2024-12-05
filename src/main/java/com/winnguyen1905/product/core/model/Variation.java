package com.winnguyen1905.product.core.model;

import io.micrometer.common.lang.NonNull;

public record Variation(@NonNull String detail, @NonNull Integer price) implements AbstractModel {}
