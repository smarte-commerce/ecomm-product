package com.winnguyen1905.product.core.model.response;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;
import lombok.Data;

@Data
public class InventoryConfirmationResponse implements AbstractModel {
  private UUID reservationId;
  private UUID orderId;
  List<Item> items;

  @Data
  @Builder
  public static class Item {
    private UUID productId;
    private UUID variantId;
    private int quantityDeducted;
    private int remainingStock;
  }
}
