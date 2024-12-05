package com.winnguyen1905.product.persistance.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
@SQLRestriction("is_deleted <> true")
@SQLDelete(sql = "UPDATE products SET is_deleted = TRUE WHERE ID=? and VERSION=?")
public class EProduct extends EBaseAudit {
  
  @Column(name = "p_name", nullable = false)
  private String name;

  @Column(name = "p_thumb", nullable = true)
  private String thumb;

  @Column(name = "p_description", nullable = true)
  private String description;

  @Column(name = "p_price", nullable = false)
  private Double price;

  @Column(name = "p_type", insertable = false, updatable = false)
  private String productType;

  @Column(name = "p_brand", nullable = false)
  private String brand;

  @Column(name = "p_slug", nullable = true, unique = true)
  private String slug;

  @Column(name = "is_draft")
  private Boolean isDraft;

  @Column(name = "is_published")
  private Boolean isPublished;

  @Column(name = "shop_id")
  private UUID shopId;

  @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
  private List<EVariation> variations = new ArrayList<>();



  // @PrePersist
  // protected void prePersist() {
  // this.isDraft = false;
  // this.isPublished = true;
  // super.prePersist();
  // }

  // @PreUpdate
  // protected void preUpdate() {
  // super.preUpdate();
  // }

}
