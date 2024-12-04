package com.winnguyen1905.product.persistance.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.winnguyen1905.product.common.ProductTypeConstant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
@SQLRestriction("is_deleted <> true")
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE products SET is_deleted = TRUE WHERE ID=? and VERSION=?")
@DiscriminatorColumn(name = "p_type", discriminatorType = DiscriminatorType.STRING)
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

  //
  // @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  // private List<CartItemEntity> cartItems = new ArrayList<>();

  // @OneToMany(mappedBy = "product")
  // private List<CommentEntity> comments = new ArrayList<>();
  //
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
