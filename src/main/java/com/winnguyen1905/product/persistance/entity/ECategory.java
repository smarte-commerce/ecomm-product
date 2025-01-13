package com.winnguyen1905.product.persistance.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "categories")
public class ECategory extends EBaseAudit {
  @Column(name = "category_name", nullable = false)
  private String name;

  private String code;

  @Column(name = "category_left")
  private Long left;

  @Column(name = "category_right")
  private Long right;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  private List<EProduct> products;

  @Column(name = "category_description")
  private String description;

  @Column(name = "category_parent_id")
  private UUID parentId;

  @Column(name = "is_published")
  private Boolean isPublished;
  
  @Column(name = "shop_id")
  private UUID shopId;

  @PrePersist
  private void prePersist() {
    if (this.isPublished == null) {
      this.isPublished = true;
    }
    if (this.products == null) {
      this.products = new ArrayList<>();
    }
  }
}
