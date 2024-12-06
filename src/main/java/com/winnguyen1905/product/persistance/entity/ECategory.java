package com.winnguyen1905.product.persistance.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class ECategory extends EBaseAudit {
  @Column(name = "category_name", nullable = false)
  private String name;

  @Column(name = "category_left")
  private Long left;

  @Column(name = "category_right")
  private Long right;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  private List<EProduct> products = new ArrayList<>();

  @Column(name = "category_description")
  private String description;

  @Column(name = "category_parent_id")
  private UUID parentId;
  
  @Column(name = "shop_id")
  private UUID shopId;
}
