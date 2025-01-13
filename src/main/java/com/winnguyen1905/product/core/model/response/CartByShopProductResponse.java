package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record CartByShopProductResponse(
    List<CartByShopProductItem> cartByShopProductItems) implements AbstractModel {
  public static record CartByShopProductItem(
      UUID shopId,
      List<ProductVariantReview> productVariantResponses) {
  }
}
