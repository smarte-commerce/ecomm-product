package com.winnguyen1905.product.persistance.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.redis.core.RedisHash;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.secure.RegionPartition;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products", schema = "public", indexes = {
    @Index(name = "idx_product_vendor_status", columnList = "vendor_id, status"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_brand", columnList = "brand_id"),
    @Index(name = "idx_product_slug", columnList = "slug"),
    @Index(name = "idx_product_region", columnList = "region"),
    @Index(name = "idx_product_published", columnList = "is_published"),
    @Index(name = "idx_product_created", columnList = "created_date"),
    @Index(name = "idx_product_price_range", columnList = "min_price, max_price")
})
@FilterDef(name = "regionFilter", parameters = @ParamDef(name = "region", type = String.class))
@Filter(name = "regionFilter", condition = "region = :region")
@FilterDef(name = "vendorFilter", parameters = @ParamDef(name = "vendorId", type = String.class))
@Filter(name = "vendorFilter", condition = "vendor_id = :vendorId")
@RedisHash(value = "product", timeToLive = 3600) // 1 hour cache
public class EProduct implements Serializable {
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

  // Basic product information
  @NotBlank(message = "Product name is required")
  @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
  @Column(name = "product_name", nullable = false, length = 255)
  private String name;

  @Size(max = 2000, message = "Description cannot exceed 2000 characters")
  @Column(name = "product_description", length = 2000)
  private String description;

  @Column(name = "slug", unique = true, length = 300)
  private String slug;

  @Column(name = "short_description", length = 500)
  private String shortDescription;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ProductStatus status = ProductStatus.DRAFT;

  @Column(name = "is_published")
  @Builder.Default
  private Boolean isPublished = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "region", nullable = false)
  private RegionPartition region;

  // Multi-vendor support
  @NotNull(message = "Vendor ID is required")
  @Column(name = "vendor_id", nullable = false)
  private UUID vendorId;

  @Column(name = "shop_id", nullable = false)
  private UUID shopId;

  // Product categorization
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id")
  private EBrand brand;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private ECategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "product_type", nullable = false)
  private ProductType productType;

  // Pricing information
  @Column(name = "min_price", precision = 19, scale = 2)
  private BigDecimal minPrice;

  @Column(name = "max_price", precision = 19, scale = 2)
  private BigDecimal maxPrice;

  @Column(name = "base_price", precision = 19, scale = 2)
  private BigDecimal basePrice;

  // SEO and marketing
  @Column(name = "meta_title", length = 255)
  private String metaTitle;

  @Column(name = "meta_description", length = 500)
  private String metaDescription;

  @Column(name = "meta_keywords", length = 500)
  private String metaKeywords;

  @Column(name = "tags", length = 1000)
  private String tags; // Comma-separated tags

  // Product features and specifications
  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "product_features")
  private Object features;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "specifications")
  private Object specifications;

  // Inventory and stock management
  @Column(name = "track_inventory")
  @Builder.Default
  private Boolean trackInventory = true;

  @Column(name = "allow_backorder")
  @Builder.Default
  private Boolean allowBackorder = false;

  @Column(name = "low_stock_threshold")
  @Builder.Default
  private Integer lowStockThreshold = 10;

  // Shipping and dimensions
  @Column(name = "weight", precision = 10, scale = 3)
  private BigDecimal weight; // in kg

  @Column(name = "length", precision = 10, scale = 2)
  private BigDecimal length; // in cm

  @Column(name = "width", precision = 10, scale = 2)
  private BigDecimal width; // in cm

  @Column(name = "height", precision = 10, scale = 2)
  private BigDecimal height; // in cm

  @Column(name = "requires_shipping")
  @Builder.Default
  private Boolean requiresShipping = true;

  // Analytics and metrics
  @Column(name = "view_count")
  @Builder.Default
  private Long viewCount = 0L;

  @Column(name = "purchase_count")
  @Builder.Default
  private Long purchaseCount = 0L;

  @Column(name = "rating_average", precision = 3, scale = 2)
  private BigDecimal ratingAverage;

  @Column(name = "rating_count")
  @Builder.Default
  private Integer ratingCount = 0;

  // Relationships
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<EProductVariant> variants = new ArrayList<>();

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Builder.Default
  private List<EInventory> inventories = new ArrayList<>();

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Builder.Default
  private List<EProductImage> images = new ArrayList<>();

  @PrePersist
  protected void prePersist() {
    if (this.isDeleted == null) {
      this.isDeleted = false;
    }
    if (this.isPublished == null) {
      this.isPublished = false;
    }
    if (this.status == null) {
      this.status = ProductStatus.DRAFT;
    }
    if (this.trackInventory == null) {
      this.trackInventory = true;
    }
    if (this.allowBackorder == null) {
      this.allowBackorder = false;
    }
    if (this.requiresShipping == null) {
      this.requiresShipping = true;
    }
    if (this.lowStockThreshold == null) {
      this.lowStockThreshold = 10;
    }
    if (this.viewCount == null) {
      this.viewCount = 0L;
    }
    if (this.purchaseCount == null) {
      this.purchaseCount = 0L;
    }
    if (this.ratingCount == null) {
      this.ratingCount = 0;
    }

    // Generate slug if not provided
    if (this.slug == null && this.name != null) {
      this.slug = generateSlug(this.name);
    }

    // Update price range based on variants
    updatePriceRange();
  }

  @PreUpdate
  protected void preUpdate() {
    // Update price range when product is updated
    updatePriceRange();

    // Update slug if name changed and slug is not manually set
    if (this.name != null && (this.slug == null || this.slug.isEmpty())) {
      this.slug = generateSlug(this.name);
    }
  }

  /**
   * Generate SEO-friendly slug from product name
   */
  public String generateSlug(String input) {
    if (input == null || input.trim().isEmpty()) {
      return "product-" + System.currentTimeMillis();
    }

    final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    final Pattern WHITESPACE = Pattern.compile("[\\s]");
    final Pattern MULTIPLEWHITESPACE = Pattern.compile("[-]{2,}");

    String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
    String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
    String slug = NONLATIN.matcher(normalized).replaceAll("");
    slug = MULTIPLEWHITESPACE.matcher(slug).replaceAll("-");

    // Remove leading/trailing dashes
    slug = slug.replaceAll("^-+|-+$", "");

    return slug.toLowerCase(Locale.ENGLISH) + "-" + System.currentTimeMillis();
  }

  /**
   * Update min and max price based on product variants
   */
  private void updatePriceRange() {
    if (variants != null && !variants.isEmpty()) {
      List<BigDecimal> prices = variants.stream()
          .filter(v -> v.getPrice() != null)
          .map(v -> BigDecimal.valueOf(v.getPrice()))
          .toList();
          
      if (!prices.isEmpty()) {
        this.minPrice = prices.stream()
            .min(BigDecimal::compareTo)
            .orElse(this.basePrice);

        this.maxPrice = prices.stream()
            .max(BigDecimal::compareTo)
            .orElse(this.basePrice);
      }
    } else if (this.basePrice != null) {
      this.minPrice = this.basePrice;
      this.maxPrice = this.basePrice;
    }
  }

  /**
   * Check if product is available for purchase
   */
  public boolean isAvailable() {
    return this.status == ProductStatus.ACTIVE &&
           this.isPublished &&
           !this.isDeleted;
  }

  /**
   * Check if product is visible to customers
   */
  public boolean isVisible() {
    return this.status.isCustomerVisible() &&
           this.isPublished &&
           !this.isDeleted;
  }

  /**
   * Check if product can be edited by vendor
   */
  public boolean isEditable() {
    return this.status.isEditable() && !this.isDeleted;
  }

  /**
   * Increment view count
   */
  public void incrementViewCount() {
    this.viewCount = (this.viewCount == null ? 0L : this.viewCount) + 1;
  }

  /**
   * Increment purchase count
   */
  public void incrementPurchaseCount() {
    this.purchaseCount = (this.purchaseCount == null ? 0L : this.purchaseCount) + 1;
  }

  /**
   * Update product rating
   */
  public void updateRating(BigDecimal newRating) {
    if (this.ratingAverage == null) {
      this.ratingAverage = newRating;
      this.ratingCount = 1;
    } else {
      BigDecimal totalRating = this.ratingAverage.multiply(BigDecimal.valueOf(this.ratingCount));
      totalRating = totalRating.add(newRating);
      this.ratingCount++;
      this.ratingAverage = totalRating.divide(BigDecimal.valueOf(this.ratingCount), 2, java.math.RoundingMode.HALF_UP);
    }
  }
}
