package com.winnguyen1905.product.persistance.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class ECategory extends EBaseAudit {
  @Column(name = "category_left")
  private Integer left;

  @Column(name = "category_right")
  private Integer right;

  @Column(name = "shop_id")
  private UUID shopId;

  @Column(name = "category_description")
  private String description;
}
