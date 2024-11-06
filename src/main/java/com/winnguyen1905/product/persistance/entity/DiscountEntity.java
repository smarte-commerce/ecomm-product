package com.winnguyen1905.product.persistance.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.winnguyen1905.product.core.common.DiscountType;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "discounts")
public class DiscountEntity extends BaseEntityAudit {
    @Version
    private Integer version;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "discount_users_used",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> customer = new ArrayList<>();

    @Min(value = 1)
    @Column(name = "discount_max_uses_per_user")
    private int maxUsesPerUser;

    @Min(value = 0)
    @Column(name = "discount_min_order_value")
    private Double minOrderValue;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private UserEntity shop;

    @Column(name = "discount_is_active")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_applies_to")
    private ApplyDiscountType appliesTo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "discount_products",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<ProductEntity> products = new HashSet<>();

    // @ManyToMany(mappedBy = "discounts")
    // private List<CartEntity> carts;

    public void addProduct(ProductEntity product) {
        this.products.add(product);
    }
    
    @Override
    @PrePersist
    protected void prePersist() {
        this.setDiscountType(this.discountType == null ? DiscountType.FIXED_AMOUNT : this.discountType);
        this.setAppliesTo(this.appliesTo == null ? ApplyDiscountType.ALL : this.appliesTo);
        this.setIsActive(this.isActive == null ? false : this.isActive);
        super.prePersist();
    }
}