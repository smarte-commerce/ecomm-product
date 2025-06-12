package com.winnguyen1905.product.core.model.viewmodel;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record ProductVariantByShopVm(
    List<ShopProductVariant> shopProductVariants) implements AbstractModel {
  @Builder
  public ProductVariantByShopVm(
      List<ShopProductVariant> shopProductVariants) {
    this.shopProductVariants = shopProductVariants;
  }

  public record ShopProductVariant(
      UUID shopId,
      List<ProductVariantReviewVm> productVariantReviews) implements AbstractModel {

    @Builder
    public ShopProductVariant(
        UUID shopId,
        List<ProductVariantReviewVm> productVariantReviews) {
      this.shopId = shopId;
      this.productVariantReviews = productVariantReviews;
    }

  }

}
