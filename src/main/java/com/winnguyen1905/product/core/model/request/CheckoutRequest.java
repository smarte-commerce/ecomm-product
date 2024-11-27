package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CheckoutRequest extends AbstractModel {
  private List<CheckoutItemRequest> checkoutItems;

  @Getter
  @Setter
  @Builder
  public static class CheckoutItemRequest extends AbstractModel {
    private UUID cartId;
    private Set<UUID> discountIds;
  }
}
