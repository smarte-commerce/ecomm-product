package com.winnguyen1905.product.core.model.viewmodel;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record ProductVariantByShopVm(
    List<ShopProductVariant> ShopProductVariants) implements AbstractModel {
  @Builder
  public ProductVariantByShopVm(
      List<ShopProductVariant> ShopProductVariants) {
    this.ShopProductVariants = ShopProductVariants;
  }

  public static record ShopProductVariant(
      UUID shopId,
      List<ProductVariantReviewVm> productVariantReviews) {

  }
}
