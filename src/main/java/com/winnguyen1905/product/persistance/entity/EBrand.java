package com.winnguyen1905.product.persistance.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.redis.core.RedisHash;

import com.winnguyen1905.product.secure.RegionPartition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "brands", schema = "public", indexes = {
    @Index(name = "idx_brand_code_vendor", columnList = "brand_code, vendor_id"),
    @Index(name = "idx_brand_name_vendor", columnList = "brand_name, vendor_id"),
    @Index(name = "idx_brand_region", columnList = "region"),
    @Index(name = "idx_brand_verified", columnList = "is_verified")
})
@FilterDef(name = "regionFilter", parameters = @ParamDef(name = "region", type = String.class))
@Filter(name = "regionFilter", condition = "region = :region")
@FilterDef(name = "vendorFilter", parameters = @ParamDef(name = "vendorId", type = String.class))
@Filter(name = "vendorFilter", condition = "vendor_id = :vendorId")
@RedisHash(value = "brand", timeToLive = 3600) // 1 hour cache
public class EBrand implements Serializable {
  
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "brand_name", nullable = false, length = 255)
  private String name;

  @Column(name = "brand_code", nullable = false, length = 100)
  private String code;

  @Column(name = "brand_description", length = 1000)
  private String description;

  @Column(name = "brand_logo_url", length = 500)
  private String logoUrl;

  @Column(name = "brand_website_url", length = 500)
  private String websiteUrl;

  @Column(name = "is_verified")
  @Builder.Default
  private Boolean isVerified = false;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Enumerated(EnumType.STRING)
  @Column(name = "region", nullable = false)
  private RegionPartition region;

  // Multi-vendor support
  @Column(name = "vendor_id", nullable = false)
  private UUID vendorId;

  @Column(name = "is_global_brand")
  @Builder.Default
  private Boolean isGlobalBrand = false;

  // Metrics for analytics
  @Column(name = "product_count")
  @Builder.Default
  private Integer productCount = 0;

  @Column(name = "total_sales")
  @Builder.Default
  private Double totalSales = 0.0;

  @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Builder.Default
  private List<EProduct> products = new ArrayList<>();

  @PrePersist
  private void prePersist() {
    if (this.isDeleted == null) {
      this.isDeleted = false;
    }
    if (this.isVerified == null) {
      this.isVerified = false;
    }
    if (this.isActive == null) {
      this.isActive = true;
    }
    if (this.isGlobalBrand == null) {
      this.isGlobalBrand = false;
    }
    if (this.productCount == null) {
      this.productCount = 0;
    }
    if (this.totalSales == null) {
      this.totalSales = 0.0;
    }
  }

  // Business methods
  public void incrementProductCount() {
    this.productCount = (this.productCount != null ? this.productCount : 0) + 1;
  }

  public void decrementProductCount() {
    this.productCount = Math.max(0, (this.productCount != null ? this.productCount : 0) - 1);
  }

  public void addSales(Double amount) {
    this.totalSales = (this.totalSales != null ? this.totalSales : 0.0) + amount;
  }

  // Unique constraint validation
  public String getUniqueKey() {
    return code + "_" + vendorId;
  }
}
