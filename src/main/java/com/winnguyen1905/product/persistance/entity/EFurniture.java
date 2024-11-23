package com.winnguyen1905.product.persistance.entity;


import com.winnguyen1905.product.core.common.ProductTypeConstant;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "furnitures")
@DiscriminatorValue(ProductTypeConstant.FURNITURE)
@PrimaryKeyJoinColumn(name = "furniture_id")
public class EFurniture extends EProduct {
    // @Column(name = "size", nullable = false)
    // private Double size;

    // @Column(name = "os", nullable = false)
    // private String os;

    // @Column(name = "ram", nullable = false)
    // private Integer ram;

    // @Column(name = "rom", nullable = false)
    // private Integer rom;

    // @Column(name = "pin", nullable = false)
    // private Integer pin;

    // @Column(name = "cpu", nullable = false)
    // private String cpu;

    // @Column(name = "gpu", nullable = false)
    // private String gpu;
}
