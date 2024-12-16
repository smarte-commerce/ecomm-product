package com.winnguyen1905.product.persistance.elasticsearch;

import org.springframework.data.elasticsearch.annotations.Field;

import com.winnguyen1905.product.persistance.entity.EBaseAudit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ESInventory extends EBaseAudit {
  @Field(name = "sku")
  String sku;

  @Field(name = "quantity_available")
  private int quantityAvailable;

  @Field(name = "quantity_reserved")
  private int quantityReserved;

  @Field(name = "quantity_sold")
  private int quantitySold;
}
