package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record ProductVariantByShopResponse(
    List<ShopProductVariant> ShopProductVariants
) implements AbstractModel {
  public static record ShopProductVariant(
      UUID shopId,
      List<ProductVariantReview> productVariantReviews) {
  }
}
