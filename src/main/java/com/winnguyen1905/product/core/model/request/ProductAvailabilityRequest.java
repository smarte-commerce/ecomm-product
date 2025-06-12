package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
public class ProductAvailabilityRequest implements AbstractModel {

  private List<Item> items;

  @Data
  @Builder
  public static class Item {
    private UUID productId;
    private UUID variantId;
    private int quantity;
  }
}
