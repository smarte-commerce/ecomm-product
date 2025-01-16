package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record CategoryTreeResponse(
    CategoryItem categoryItem,
    List<CategoryItem> categoryChilds) implements AbstractModel {
  public static final record CategoryItem(
      UUID id,
      String name,
      String description) {
  }
}
