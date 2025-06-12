package com.winnguyen1905.product.persistance.entity;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.config.JsonNodeConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_variant", schema = "public")
public class EProductVariant implements Serializable {
  @Id
  // @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "sku")
  private String sku;

  @Column(name = "variation_price")
  private Double price;

  @Column(columnDefinition = "jsonb", name = "features")
  @Convert(converter = JsonNodeConverter.class)
  private JsonNode features;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private EProduct product;

  @PrePersist
  protected void prePersist() {
    this.isDeleted = false;
    this.version = 0;
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
