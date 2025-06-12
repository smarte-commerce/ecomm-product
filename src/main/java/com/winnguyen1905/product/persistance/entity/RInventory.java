package com.winnguyen1905.product.persistance.entity;

import java.util.UUID;

import org.springframework.data.redis.core.RedisHash;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@RedisHash(value = "inventory")
public class RInventory {
  private UUID productId;
  private UUID variationId;
  private EInventory inventory;
}
