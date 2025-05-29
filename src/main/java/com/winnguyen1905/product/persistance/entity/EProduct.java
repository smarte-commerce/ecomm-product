package com.winnguyen1905.product.persistance.entity;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.*;

import com.winnguyen1905.product.secure.RegionPartition;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product", schema = "ecommerce")
@FilterDef(name = "regionFilter", parameters = @ParamDef(name = "region", type = String.class))
@Filter(name = "regionFilter", condition = "region = :region")
@SQLRestriction("is_deleted <> true")
@SQLDelete(sql = "UPDATE ecommerce.product SET is_deleted = TRUE WHERE id=? and version=?")
public class EProduct implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "is_deleted", columnDefinition = "BIT(1)")
  private Boolean isDeleted;

  @CreationTimestamp
  @Column(name = "created_date", nullable = false)
  private Instant createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "slug", unique = true)
  private String slug;

  @Column(name = "is_published", columnDefinition = "BIT(1)")
  private boolean isPublished;

  @Column(name = "shop_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID shopId;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<EProductVariant> variations;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<EInventory> inventories;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<EProductImage> images;

  @ManyToOne
  @JoinColumn(name = "brand_id", columnDefinition = "BINARY(16)")
  private EBrand brand;

  @ManyToOne
  @JoinColumn(name = "category_id", columnDefinition = "BINARY(16)")
  private ECategory category;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "product_features")
  private Object features;

  // @Column(name = "is_verified_by_brand", columnDefinition = "BIT(1)")
  // private boolean isVerifiedByBrand;

  @PrePersist
  protected void prePersist() {
    // this.isVerifiedByBrand = false;
    this.isPublished = true;
  }

  public String generateSlug() {
    final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    final Pattern WHITESPACE = Pattern.compile("[\\s]");

    String nowhitespace = WHITESPACE.matcher(this.name).replaceAll("-"),
        normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD),
        slug = NONLATIN.matcher(normalized).replaceAll("") + '-';

    return slug.toLowerCase(Locale.ENGLISH) + System.currentTimeMillis();
  }
}
