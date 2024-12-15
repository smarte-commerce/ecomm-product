package com.winnguyen1905.product.persistance.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import com.winnguyen1905.product.common.ProductImageType;
import com.winnguyen1905.product.common.ProductType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
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

  @Column(name = "p_description", nullable = true)
  private String description;

  @Column(name = "p_slug", nullable = true, unique = true)
  private String slug;

  @Column(name = "is_draft")
  private boolean isDraft;

  @Column(name = "is_published")
  private boolean isPublished;

  @Column(name = "shop_id")
  private UUID shopId;

  @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  private List<EVariation> variations = new ArrayList<>();

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
  private List<EInventory> inventories = new ArrayList<>();

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
  private List<EProductImage> images = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "brand_id")
  private EBrand brand;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private ECategory category;

  @Column(name = "p_type")
  @Enumerated(EnumType.STRING)
  private ProductType productType;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "product_features")
  private JsonNode features;

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
