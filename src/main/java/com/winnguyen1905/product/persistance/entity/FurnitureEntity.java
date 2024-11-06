package com.winnguyen1905.technologystore.entity;

import com.winnguyen1905.technologystore.common.ProductTypeConstant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "furnitures")
@DiscriminatorValue(ProductTypeConstant.FURNITURE)
@PrimaryKeyJoinColumn(name = "furniture_id")
public class FurnitureEntity extends ProductEntity {
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