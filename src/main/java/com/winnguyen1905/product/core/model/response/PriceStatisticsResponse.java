package com.winnguyen1905.product.core.model.response;

import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PriceStatisticsResponse extends AbstractModel {
  private Double totalPrice;
  private Double totalShipPrice;
  private UUID discountId;
  private Double totalDiscountVoucher;

  private Double amountShipReduced;
  private Double amountProductReduced;

  private Double finalPrice;
}
