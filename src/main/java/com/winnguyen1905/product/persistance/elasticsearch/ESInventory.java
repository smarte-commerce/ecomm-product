package com.winnguyen1905.product.persistance.elasticsearch;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ESInventory {
  @Id
  private UUID id;

  @Field(name = "sku")
  private String sku;

  @Field(name = "quantity_available")
  private int quantityAvailable;

  @Field(name = "quantity_reserved")
  private int quantityReserved;

  @Field(name = "quantity_sold")
  private int quantitySold;
}
