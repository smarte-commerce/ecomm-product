package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class ProductAvailabilityResponse implements AbstractModel {

  private List<Item> items;

  @Data
  @Builder
  public static class Item {
    private UUID productId;
    private UUID variantId;
    private boolean available;
    private boolean isActive;
    private Integer stockQuantity;
    private double currentPrice; 
  }
}
