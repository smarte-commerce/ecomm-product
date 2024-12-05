package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record CheckoutResponse(PriceStatisticsResponse priceStatistics,
    List<CheckoutItemReponse> checkoutItems) implements AbstractModel {

  public record CheckoutItemReponse(UUID cartId, PriceStatisticsResponse priceStatistics)
      implements AbstractModel {
  }
}
