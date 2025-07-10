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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.redis.core.RedisHash;

import com.winnguyen1905.product.secure.RegionPartition;

import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "categories", schema = "public", indexes = {
    @Index(name = "idx_category_code_vendor", columnList = "category_code, vendor_id"),
    @Index(name = "idx_category_parent", columnList = "parent_id"),
    @Index(name = "idx_category_level", columnList = "category_level"),
    @Index(name = "idx_category_published", columnList = "is_published"),
    @Index(name = "idx_category_region", columnList = "region")
})
@FilterDef(name = "regionFilter", parameters = @ParamDef(name = "region", type = String.class))
@Filter(name = "regionFilter", condition = "region = :region")
@FilterDef(name = "vendorFilter", parameters = @ParamDef(name = "vendorId", type = String.class))
@Filter(name = "vendorFilter", condition = "vendor_id = :vendorId")
@RedisHash(value = "category", timeToLive = 3600) // 1 hour cache
public class ECategory implements Serializable {
  
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @Column(name = "is_deleted")
  @Builder.Default
  private Boolean isDeleted = false;

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

  @Column(name = "category_name", nullable = false, length = 255)
  private String name;

  @Column(name = "category_code", nullable = false, length = 100)
  private String code;

  @Column(name = "category_description", length = 1000)
  private String description;

  @Column(name = "category_image_url", length = 500)
  private String imageUrl;

  @Column(name = "category_icon_url", length = 500)
  private String iconUrl;

  @Column(name = "is_published")
  @Builder.Default
  private Boolean isPublished = true;

  @Column(name = "is_featured")
  @Builder.Default
  private Boolean isFeatured = false;

  @Column(name = "display_order")
  @Builder.Default
  private Integer displayOrder = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "region", nullable = false)
  private RegionPartition region;

  // Multi-vendor support
  @Column(name = "vendor_id", nullable = false)
  private UUID vendorId;

  @Column(name = "is_global_category")
  @Builder.Default
  private Boolean isGlobalCategory = false;

  // Hierarchical structure
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private ECategory parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<ECategory> children = new ArrayList<>();

  @Column(name = "category_level")
  @Builder.Default
  private Integer categoryLevel = 0;

  @Column(name = "category_path", length = 1000)
  private String categoryPath; // e.g., "Electronics/Computers/Laptops"

  // Product features template
  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "product_features_template")
  private Object productFeaturesTemplate;

  // SEO fields
  @Column(name = "meta_title", length = 255)
  private String metaTitle;

  @Column(name = "meta_description", length = 500)
  private String metaDescription;

  @Column(name = "meta_keywords", length = 500)
  private String metaKeywords;

  @Column(name = "slug", length = 255)
  private String slug;

  // Analytics
  @Column(name = "product_count")
  @Builder.Default
  private Integer productCount = 0;

  @Column(name = "total_sales")
  @Builder.Default
  private Double totalSales = 0.0;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  @Builder.Default
  private List<EProduct> products = new ArrayList<>();

  @PrePersist
  private void prePersist() {
    if (this.isDeleted == null) {
      this.isDeleted = false;
    }
    if (this.isPublished == null) {
      this.isPublished = true;
    }
    if (this.isFeatured == null) {
      this.isFeatured = false;
    }
    if (this.isGlobalCategory == null) {
      this.isGlobalCategory = false;
    }
    if (this.displayOrder == null) {
      this.displayOrder = 0;
    }
    if (this.categoryLevel == null) {
      this.categoryLevel = 0;
    }
    if (this.productCount == null) {
      this.productCount = 0;
    }
    if (this.totalSales == null) {
      this.totalSales = 0.0;
    }
    
    // Generate slug if not provided
    if (this.slug == null && this.name != null) {
      this.slug = generateSlug(this.name);
    }
    
    // Calculate category path
    updateCategoryPath();
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

  public boolean isRootCategory() {
    return this.parent == null;
  }

  public boolean hasChildren() {
    return this.children != null && !this.children.isEmpty();
  }

  public boolean isLeafCategory() {
    return !hasChildren();
  }

  public String getUniqueKey() {
    return code + "_" + vendorId;
  }

  private void updateCategoryPath() {
    if (this.parent == null) {
      this.categoryPath = this.name;
      this.categoryLevel = 0;
    } else {
      this.categoryPath = this.parent.getCategoryPath() + "/" + this.name;
      this.categoryLevel = this.parent.getCategoryLevel() + 1;
    }
  }

  private String generateSlug(String name) {
    return name.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

  // Get all descendant categories
  public List<ECategory> getAllDescendants() {
    List<ECategory> descendants = new ArrayList<>();
    if (this.children != null) {
      for (ECategory child : this.children) {
        descendants.add(child);
        descendants.addAll(child.getAllDescendants());
      }
    }
    return descendants;
  }

  // Get all ancestor categories
  public List<ECategory> getAllAncestors() {
    List<ECategory> ancestors = new ArrayList<>();
    ECategory current = this.parent;
    while (current != null) {
      ancestors.add(0, current); // Add at beginning to maintain order
      current = current.getParent();
    }
    return ancestors;
  }
}
