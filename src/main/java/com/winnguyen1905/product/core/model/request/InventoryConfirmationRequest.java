package com.winnguyen1905.product.core.model.request;

import java.util.UUID;

import lombok.Data;

@Data
public class InventoryConfirmationRequest implements AbstractModel {
  private UUID reservationId;
  private UUID orderId;
}
