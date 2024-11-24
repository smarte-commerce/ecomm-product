package com.winnguyen1905.product.persistance.entity;

import java.util.UUID;

import org.springframework.data.redis.core.RedisHash;

import lombok.*;

@Getter
@Setter
@Builder
@RedisHash(value = "inventory")
public class RInventory extends EBase {
  private UUID variationId;
  // Inventory inventory;
}
