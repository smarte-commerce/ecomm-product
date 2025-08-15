package com.winnguyen1905.product.persistance.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.redis.core.RedisHash;


import com.winnguyen1905.product.secure.RegionPartition;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_variants", schema = "public", indexes = {
    @Index(name = "idx_variant_product", columnList = "product_id"),
    @Index(name = "idx_variant_sku", columnList = "sku"),
    @Index(name = "idx_variant_price", columnList = "variant_price"),
    @Index(name = "idx_variant_vendor", columnList = "vendor_id"),
    @Index(name = "idx_variant_region", columnList = "region"),
    @Index(name = "idx_variant_active", columnList = "is_active")
})
@Filter(name = "regionFilter", condition = "region = :region")
@Filter(name = "vendorFilter", condition = "vendor_id = :vendorId")
@RedisHash(value = "product_variant", timeToLive = 1800) // 30 minutes cache
public class EProductVariant implements Serializable {

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

  // Basic variant information
  @NotBlank(message = "SKU is required")
  @Size(max = 100, message = "SKU cannot exceed 100 characters")
  @Column(name = "sku", unique = true, length = 100)
  private String sku;

  @Column(name = "variant_name", length = 255)
  private String name;

  @Column(name = "variant_description", length = 1000)
  private String description;

  // Pricing
  @NotNull(message = "Price is required")
  // @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
  @Column(name = "variant_price", nullable = false)
  private Double price;

  @Column(name = "compare_at_price", precision = 19, scale = 2)
  private BigDecimal compareAtPrice; // Original price for discount display

  @Column(name = "cost_price", precision = 19, scale = 2)
  private BigDecimal costPrice; // Cost for vendor analytics

  // Multi-vendor support
  @Enumerated(EnumType.STRING)
  @Column(name = "region", nullable = false)
  private RegionPartition region;

  @NotNull(message = "Vendor ID is required")
  @Column(name = "vendor_id", nullable = false)
  private UUID vendorId;

  // Status and availability
  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "is_default")
  @Builder.Default
  private Boolean isDefault = false;

  // Physical properties
  @Column(name = "weight", precision = 10, scale = 3)
  private BigDecimal weight; // in kg

  @Column(name = "length", precision = 10, scale = 2)
  private BigDecimal length; // in cm

  @Column(name = "width", precision = 10, scale = 2)
  private BigDecimal width; // in cm

  @Column(name = "height", precision = 10, scale = 2)
  private BigDecimal height; // in cm

  // Variant attributes (color, size, etc.)
  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "variant_attributes")
  private Object attributes;

  // Additional features specific to this variant
  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "variant_features")
  private Object features;

  // Inventory tracking
  @Column(name = "track_inventory")
  @Builder.Default
  private Boolean trackInventory = true;

  @Column(name = "inventory_quantity")
  @Builder.Default
  private Integer inventoryQuantity = 0;

  @Column(name = "reserved_quantity")
  @Builder.Default
  private Integer reservedQuantity = 0;

  // Analytics
  @Column(name = "view_count")
  @Builder.Default
  private Long viewCount = 0L;

  @Column(name = "purchase_count")
  @Builder.Default
  private Long purchaseCount = 0L;

  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private EProduct product;

  @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<EProductImage> images = new ArrayList<>();

  @PrePersist
  protected void prePersist() {
    if (this.isDeleted == null) {
      this.isDeleted = false;
    }
    if (this.isActive == null) {
      this.isActive = true;
    }
    if (this.isDefault == null) {
      this.isDefault = false;
    }
    if (this.trackInventory == null) {
      this.trackInventory = true;
    }
    if (this.inventoryQuantity == null) {
      this.inventoryQuantity = 0;
    }
    if (this.reservedQuantity == null) {
      this.reservedQuantity = 0;
    }
    if (this.viewCount == null) {
      this.viewCount = 0L;
    }
    if (this.purchaseCount == null) {
      this.purchaseCount = 0L;
    }

    // Copy region and vendor from product if not set
    if (this.product != null) {
      if (this.region == null) {
        this.region = this.product.getRegion();
      }
      if (this.vendorId == null) {
        this.vendorId = this.product.getVendorId();
      }
    }
  }

  @PreUpdate
  protected void preUpdate() {
    // Ensure consistency with parent product
    if (this.product != null) {
      if (this.region == null) {
        this.region = this.product.getRegion();
      }
      if (this.vendorId == null) {
        this.vendorId = this.product.getVendorId();
      }
    }
  }

  /**
   * Check if variant is available for purchase
   */
  public boolean isAvailable() {
    return this.isActive &&
           !this.isDeleted &&
           this.product != null &&
           this.product.isAvailable() &&
           (!this.trackInventory || this.getAvailableQuantity() > 0);
  }

  /**
   * Get available quantity (total - reserved)
   */
  public Integer getAvailableQuantity() {
    if (!this.trackInventory) {
      return Integer.MAX_VALUE; // Unlimited if not tracking
    }
    return Math.max(0, (this.inventoryQuantity != null ? this.inventoryQuantity : 0) -
                      (this.reservedQuantity != null ? this.reservedQuantity : 0));
  }

  /**
   * Reserve inventory quantity
   */
  public boolean reserveQuantity(Integer quantity) {
    if (!this.trackInventory) {
      return true; // Always successful if not tracking
    }

    if (quantity <= 0) {
      return false;
    }

    if (getAvailableQuantity() >= quantity) {
      this.reservedQuantity = (this.reservedQuantity != null ? this.reservedQuantity : 0) + quantity;
      return true;
    }

    return false;
  }

  /**
   * Release reserved inventory quantity
   */
  public void releaseReservedQuantity(Integer quantity) {
    if (quantity > 0 && this.reservedQuantity != null) {
      this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }
  }

  /**
   * Confirm sale and update inventory
   */
  public boolean confirmSale(Integer quantity) {
    if (!this.trackInventory) {
      incrementPurchaseCount();
      return true;
    }

    if (quantity <= 0) {
      return false;
    }

    // Reduce from reserved first, then from available
    int fromReserved = Math.min(quantity, this.reservedQuantity != null ? this.reservedQuantity : 0);
    int fromAvailable = quantity - fromReserved;

    if (fromAvailable > 0 && getAvailableQuantity() < fromAvailable) {
      return false; // Not enough inventory
    }

    // Update quantities
    if (fromReserved > 0) {
      this.reservedQuantity = (this.reservedQuantity != null ? this.reservedQuantity : 0) - fromReserved;
    }

    this.inventoryQuantity = (this.inventoryQuantity != null ? this.inventoryQuantity : 0) - quantity;
    incrementPurchaseCount();

    return true;
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
   * Check if this variant has a discount
   */
  public boolean hasDiscount() {
    return this.compareAtPrice != null && this.price != null &&
           this.compareAtPrice.compareTo(BigDecimal.valueOf(this.price)) > 0;
  }

  /**
   * Calculate discount percentage
   */
  public BigDecimal getDiscountPercentage() {
    if (!hasDiscount()) {
      return BigDecimal.ZERO;
    }

    BigDecimal priceAsBigDecimal = BigDecimal.valueOf(this.price);
    BigDecimal discount = this.compareAtPrice.subtract(priceAsBigDecimal);
    return discount.divide(this.compareAtPrice, 4, java.math.RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Calculate profit margin
   */
  public BigDecimal getProfitMargin() {
    if (this.costPrice == null || this.costPrice.compareTo(BigDecimal.ZERO) <= 0 || this.price == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal priceAsBigDecimal = BigDecimal.valueOf(this.price);
    BigDecimal profit = priceAsBigDecimal.subtract(this.costPrice);
    return profit.divide(priceAsBigDecimal, 4, java.math.RoundingMode.HALF_UP)
                 .multiply(BigDecimal.valueOf(100));
  }
}
