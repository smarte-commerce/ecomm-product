package com.winnguyen1905.product.persistance.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.redis.core.RedisHash;

import com.winnguyen1905.product.common.constant.ProductImageType;

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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_images", schema = "public", indexes = {
    @Index(name = "idx_product_image_product", columnList = "product_id"),
    @Index(name = "idx_product_image_variant", columnList = "product_variant_id"),
    @Index(name = "idx_product_image_type", columnList = "image_type"),
    @Index(name = "idx_product_image_order", columnList = "display_order"),
    @Index(name = "idx_product_image_vendor", columnList = "vendor_id")
})
@FilterDef(name = "vendorFilter", parameters = @ParamDef(name = "vendorId", type = String.class))
@Filter(name = "vendorFilter", condition = "vendor_id = :vendorId")
@RedisHash(value = "product_image", timeToLive = 1800) // 30 minutes cache
public class EProductImage implements Serializable {
  
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

  // Product relationship
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private EProduct product;

  // Optional variant relationship (for variant-specific images)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_variant_id")
  private EProductVariant variant;

  // Multi-vendor support
  @Column(name = "vendor_id", nullable = false)
  private UUID vendorId;

  // Image details
  @Column(name = "image_url", nullable = false, length = 1000)
  private String url;

  @Column(name = "image_alt_text", length = 255)
  private String altText;

  @Column(name = "image_title", length = 255)
  private String title;

  @Column(name = "image_description", length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "image_type", nullable = false)
  private ProductImageType type;

  @Column(name = "display_order")
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "is_primary")
  @Builder.Default
  private Boolean isPrimary = false;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  // Technical details
  @Column(name = "file_name", length = 255)
  private String fileName;

  @Column(name = "file_size")
  private Long fileSize; // in bytes

  @Column(name = "mime_type", length = 100)
  private String mimeType;

  @Column(name = "width")
  private Integer width;

  @Column(name = "height")
  private Integer height;

  // CDN and storage details
  @Column(name = "cdn_url", length = 1000)
  private String cdnUrl;

  @Column(name = "thumbnail_url", length = 1000)
  private String thumbnailUrl;

  @Column(name = "small_url", length = 1000)
  private String smallUrl;

  @Column(name = "medium_url", length = 1000)
  private String mediumUrl;

  @Column(name = "large_url", length = 1000)
  private String largeUrl;

  @Column(name = "storage_provider", length = 50)
  @Builder.Default
  private String storageProvider = "S3"; // S3, CloudFront, etc.

  @Column(name = "storage_bucket", length = 100)
  private String storageBucket;

  @Column(name = "storage_key", length = 500)
  private String storageKey;

  // SEO and metadata
  @Column(name = "color_palette", length = 500)
  private String colorPalette; // JSON array of dominant colors

  @Column(name = "is_optimized")
  @Builder.Default
  private Boolean isOptimized = false;

  @Column(name = "compression_quality")
  private Integer compressionQuality;

  // Analytics
  @Column(name = "view_count")
  @Builder.Default
  private Long viewCount = 0L;

  @Column(name = "click_count")
  @Builder.Default
  private Long clickCount = 0L;

  @PrePersist
  private void prePersist() {
    if (this.isDeleted == null) {
      this.isDeleted = false;
    }
    if (this.isPrimary == null) {
      this.isPrimary = false;
    }
    if (this.isActive == null) {
      this.isActive = true;
    }
    if (this.displayOrder == null) {
      this.displayOrder = 0;
    }
    if (this.isOptimized == null) {
      this.isOptimized = false;
    }
    if (this.viewCount == null) {
      this.viewCount = 0L;
    }
    if (this.clickCount == null) {
      this.clickCount = 0L;
    }
    if (this.storageProvider == null) {
      this.storageProvider = "S3";
    }

    // Generate alt text if not provided
    if (this.altText == null && this.product != null) {
      this.altText = this.product.getName() + " - " + this.type.toString().toLowerCase();
    }
  }

  // Business methods
  public void incrementViewCount() {
    this.viewCount = (this.viewCount != null ? this.viewCount : 0L) + 1;
  }

  public void incrementClickCount() {
    this.clickCount = (this.clickCount != null ? this.clickCount : 0L) + 1;
  }

  public String getOptimalUrl() {
    // Return the best available URL based on priority
    if (this.cdnUrl != null && !this.cdnUrl.isEmpty()) {
      return this.cdnUrl;
    }
    return this.url;
  }

  public String getResponsiveUrl(String size) {
    return switch (size.toLowerCase()) {
      case "thumbnail", "thumb" -> this.thumbnailUrl != null ? this.thumbnailUrl : this.url;
      case "small", "sm" -> this.smallUrl != null ? this.smallUrl : this.url;
      case "medium", "md" -> this.mediumUrl != null ? this.mediumUrl : this.url;
      case "large", "lg" -> this.largeUrl != null ? this.largeUrl : this.url;
      default -> getOptimalUrl();
    };
  }

  public boolean isVariantSpecific() {
    return this.variant != null;
  }

  public boolean isProductLevel() {
    return this.variant == null;
  }

  /**
   * Get product variant ID for backward compatibility
   */
  public UUID getProductVariantId() {
    return this.variant != null ? this.variant.getId() : null;
  }

  public double getAspectRatio() {
    if (this.width != null && this.height != null && this.height != 0) {
      return (double) this.width / this.height;
    }
    return 1.0; // Default square aspect ratio
  }

  public String getFileSizeFormatted() {
    if (this.fileSize == null) {
      return "Unknown";
    }
    
    long bytes = this.fileSize;
    if (bytes < 1024) return bytes + " B";
    
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp - 1) + "i";
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
  }

  public String getDimensions() {
    if (this.width != null && this.height != null) {
      return this.width + "x" + this.height;
    }
    return "Unknown";
  }
}
