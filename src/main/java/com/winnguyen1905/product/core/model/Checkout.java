package com.winnguyen1905.product.core.model;

import java.util.List;
import java.util.Set;

import com.winnguyen1905.product.core.model.response.PriceStatisticsResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Checkout extends AbstractModel {
  // private PriceStatisticsResponse PriceStatistics;
  // private List<CheckoutItem> checkoutItems;

  // @Getter
  // @Setter
  // @Builder
  // public static class CheckoutItem extends AbstractModel {
  //   // private Cart cart;
  //   private Discount bestVoucher;
  //   private Set<Discount> discounts;
  //   private PriceStatisticsResponse PriceStatistics;
  // }
}
