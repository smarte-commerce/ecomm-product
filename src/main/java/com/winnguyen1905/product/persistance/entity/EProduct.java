package com.winnguyen1905.product.persistance.entity;

import java.io.Serializable;
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

import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.persistance.entity.garbage.EBaseAudit;
import com.winnguyen1905.product.secure.RegionPartition;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product", schema = "public")
@FilterDef(name = "regionFilter", parameters = @ParamDef(name = "region", type = String.class))
@Filter(name = "regionFilter", condition = "region = :region")
// @SQLRestriction("is_deleted <> true")
// @SQLDelete(sql = "UPDATE public.product SET is_deleted = TRUE WHERE id=? and
// version=?")
public class EProduct implements Serializable {
  @Id
  // @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RegionPartition region;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "is_deleted")
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

  @Column(name = "is_published")
  private boolean isPublished;

  @Column(name = "shop_id", nullable = false)
  private UUID shopId;

  @Default
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<EProductVariant> variations = new ArrayList<>();

  @Default
  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<EInventory> inventories = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ProductType productType;

  // @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade =
  // CascadeType.ALL)
  // private List<EProductImage> images;

  // @ManyToOne
  // @JoinColumn(name = "brand_id", columnDefinition = "BINARY(16)")
  // private EBrand brand;

  // @ManyToOne
  // @JoinColumn(name = "category_id", columnDefinition = "BINARY(16)")
  // private ECategory category;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", name = "product_features")
  private Object features;

  @PrePersist
  protected void prePersist() {
    this.isPublished = true;

    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
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
