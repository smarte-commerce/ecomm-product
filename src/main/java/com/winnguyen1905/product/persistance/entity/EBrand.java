package com.winnguyen1905.product.persistance.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brands")
public class EBrand extends EBaseAudit {
  @Column(name = "brand_name", unique = true, nullable = false)
  private String name;

  private String code;

  @Column(name = "brand_category", nullable = true)
  private String description;

  @Column(name = "brand_is_verified")
  private Boolean isVerified;

  @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
  private List<EProduct> products;

  @PrePersist
  private void prePersist() {
    this.isVerified = false;
    if (this.products == null) {
      this.products = new ArrayList<>();
    }
  }
}
