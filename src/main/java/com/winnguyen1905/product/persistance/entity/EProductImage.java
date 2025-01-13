package com.winnguyen1905.product.persistance.entity;

import java.util.UUID;

import com.winnguyen1905.product.common.ProductImageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "product_images")
public class EProductImage extends EBaseAudit {
  @Column(name = "product_variant_id")
  private UUID productVariantId;

  @Column(name = "image_url")
  private String url;

  @Column(name = "image_type")
  @Enumerated(value = EnumType.STRING)
  private ProductImageType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private EProduct product;
}
