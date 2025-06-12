package com.winnguyen1905.product.persistance.entity.garbage;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.product.common.constant.ProductImageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_image", schema = "ecommerce")
public class EProductImage {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "product_variant_id", columnDefinition = "BINARY(16)")
  private UUID productVariantId;

  @Column(name = "image_url")
  private String url;

  @Column(name = "image_type")
  @Enumerated(EnumType.STRING)
  private ProductImageType type;

  // @ManyToOne(fetch = FetchType.LAZY)
  // @JoinColumn(name = "product_id", columnDefinition = "BINARY(16)")
  // private EProduct product;
}
