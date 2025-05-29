package com.winnguyen1905.product.core.model.viewmodel;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record CategoryTreeVm(
    CategoryItem categoryItem,
    List<CategoryItem> categoryChilds
) implements AbstractModel {

  @Builder
  public CategoryTreeVm(
      UUID id,
      String name,
      String description,
      List<CategoryItem> categoryChilds) {
    this(new CategoryItem(id, name, description), categoryChilds);
  }

  public static final record CategoryItem(
      UUID id,
      String name,
      String description) {
  }
}
