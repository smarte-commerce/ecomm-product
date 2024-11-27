package com.winnguyen1905.product.persistance.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.winnguyen1905.product.common.ApplyDiscountType;
import com.winnguyen1905.product.common.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "discounts")
public class EDiscount extends EBaseAudit {

  public static enum Scope {
    SHOP, GLOBAL
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_scope")
  private Scope scope;

  @Column(name = "discount_name")
  private String name;

  @Column(name = "discount_description", columnDefinition = "MEDIUMTEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type")
  private DiscountType discountType;

  @Min(value = 0)
  @Column(name = "discount_value")
  private Double value;

  @Column(name = "discount_code")
  private String code;

  @Column(name = "discount_start_day")
  private Instant startDate;

  @Column(name = "discount_end_date")
  private Instant endDate;

  @Min(value = 1)
  @Column(name = "discount_max_uses")
  private int maxUses;

  @Min(value = 0)
  @Column(name = "discount_uses_count")
  private int usesCount;

  @Min(value = 1)
  @Column(name = "discount_max_uses_per_user")
  private int maxUsesPerUser;

  @Min(value = 0)
  @Column(name = "discount_min_order_value")
  private Double minOrderValue;

  @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY)
  private List<EUserDiscount> userDiscounts = new ArrayList<>();

  @Column(name = "discount_is_active")
  private Boolean isActive;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_applies_to")
  private ApplyDiscountType appliesTo;
  
  @ManyToMany(mappedBy = "discounts")
  private Set<EProduct> products = new HashSet<>();

  @Column(name = "shop_id", nullable = false)
  private UUID shopId;

  @PrePersist
  protected void prePersist() {
    // this.setDiscountType(this.discountType == null ? DiscountType.FIXED_AMOUNT :
    // this.discountType);
    // this.setAppliesTo(this.appliesTo == null ? ApplyDiscountType.ALL :
    // this.appliesTo);
    // this.setIsActive(this.isActive == null ? false : this.isActive);
    // super.prePersist();
  }
}
