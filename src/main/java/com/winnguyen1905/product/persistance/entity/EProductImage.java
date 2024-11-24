package com.winnguyen1905.product.persistance.entity;

import com.winnguyen1905.product.common.ProductImageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_images")
public class EProductImage extends EBaseAudit {
  @Column(name = "image_url")
  String imageUrl;

  @Column(name = "image_type")
  @Enumerated(value = EnumType.STRING)
  private ProductImageType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private EProduct product;
}
