package com.winnguyen1905.technologystore.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.*;
import com.winnguyen1905.technologystore.common.ProductTypeConstant;
import com.winnguyen1905.technologystore.entity.base.BaseEntityAudit;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "products")
@SQLRestriction("is_deleted <> true")
@SQLDelete(sql = "UPDATE products SET is_deleted = TRUE WHERE ID=? and VERSION=?")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "p_type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ElectronicEntity.class, name = ProductTypeConstant.ELECTRONIC),
    @JsonSubTypes.Type(value = FurnitureEntity.class, name = ProductTypeConstant.FURNITURE),
    @JsonSubTypes.Type(value = ClothingEntity.class, name = ProductTypeConstant.CLOTHING),
    @JsonSubTypes.Type(value = FootwearEntity.class, name = ProductTypeConstant.FOOTWEAR)
})
// @Index(columnList = "name")
public class ProductEntity extends BaseEntityAudit {

    @Version
    private Integer version;
    
    @Column(name = "p_name", nullable = false)
    private String name;

    @Column(name = "p_thumb", nullable = true)
    private String thumb;

    @Column(name = "p_description", nullable = true)
    private String description;

    @Column(name = "p_price", nullable = false)
    private Double price;

    @Column(name = "p_type", insertable = false, updatable = false)
    private String productType;

    @Column(name = "p_brand", nullable = false)
    private String brand;

    @Column(name = "p_slug", nullable = true, unique = true)
    private String slug;

    @Column(name = "is_draft")
    private Boolean isDraft;

    @Column(name = "is_published")
    private Boolean isPublished;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private UserEntity shop;

    @ManyToMany(mappedBy = "products")
    private Set<DiscountEntity> discounts  = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<CartItemEntity> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<VariationEntity> variations = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<CommentEntity> comments = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        this.isDraft = false;
        this.isPublished = true;
        super.prePersist();
    }

    @PreUpdate
    protected void preUpdate() {
        super.preUpdate();
    }

}