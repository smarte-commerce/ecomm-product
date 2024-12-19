package com.winnguyen1905.product.core.service;

import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import io.micrometer.common.lang.Nullable;

public record ServiceParamFormat<T extends AbstractModel> (
  @Nullable UUID userId,
  @Nullable T requestObject
) {}
