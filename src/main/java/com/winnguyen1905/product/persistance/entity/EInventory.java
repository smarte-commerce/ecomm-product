package com.winnguyen1905.product.persistance.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventories")
public class EInventory extends EBaseAudit {

  @Column(name = "sku")
  String sku;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private EProduct product;

  @ManyToOne
  @JoinColumn(name = "variant_id")
  private EVariation variant;

  @Column(name = "quantity_available")
  private int quantityAvailable;

  @Column(name = "quantity_reserved")
  private int quantityReserved;

  @Column(name = "quantity_sold")
  private int quantitySold;

}
