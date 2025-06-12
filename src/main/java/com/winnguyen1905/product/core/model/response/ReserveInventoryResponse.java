package com.winnguyen1905.product.core.model.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReserveInventoryResponse implements AbstractModel {
  private UUID reservationId;
  private boolean status;
  private List<Item> items;

  @Data
  @Builder
  public static class Item {
    private UUID productId;
    private UUID variantId;
    private int quantity;
  }

  private Instant expiresAt;
}
