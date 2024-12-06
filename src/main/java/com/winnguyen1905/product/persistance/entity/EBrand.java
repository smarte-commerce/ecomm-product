package com.winnguyen1905.product.persistance.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "brand")
public class EBrand extends EBaseAudit {
  @Column(name = "brand_name", unique = true, nullable = false)
  private String name;

  @Column(name = "brand_category", nullable = true)
  private String description;

  @Column(name = "brand_is_verified")
  private boolean isVerified;

  @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
  private List<EProduct> products = new ArrayList<>();
}
