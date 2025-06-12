package com.winnguyen1905.product.persistance.entity.garbage;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brands", schema = "ecommerce")
public class EBrand implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Version
  private long version;

  @Column(name = "is_deleted", updatable = true)
  private Boolean isDeleted;

  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @Column(name = "updated_by", nullable = true)
  private String updatedBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date", updatable = true)
  private Instant updatedDate;

  @Column(name = "brand_name", unique = true, nullable = false)
  private String name;

  @Column(name = "brand_code", unique = true, nullable = false)
  private String code;

  @Column(name = "brand_category", nullable = true)
  private String description;

  @Column(name = "brand_is_verified")
  private Boolean isVerified;

  // @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
  // private List<EProduct> products;

  // @PrePersist
  // private void prePersist() {
  //   this.isVerified = false;
  //   if (this.products == null) {
  //     this.products = new ArrayList<>();
  //   }
  // }
}
