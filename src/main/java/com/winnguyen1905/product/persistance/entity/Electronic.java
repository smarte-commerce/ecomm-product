package com.winnguyen1905.product.persistance.entity;

import com.winnguyen1905.product.core.common.ProductTypeConstant;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "electronics")
@DiscriminatorValue(ProductTypeConstant.ELECTRONIC)
@PrimaryKeyJoinColumn(name = "electronic_id")
public class Electronic extends EProduct {
  @Column(name = "x_size", nullable = true)
  private Double xSize;

  @Column(name = "y_size", nullable = true)
  private Double ySize;

  @Column(name = "panel_type", nullable = false)
  private String panelType;

  @Column(name = "resolution", nullable = true)
  private String resolution;

  @Column(name = "os", nullable = false)
  private String os;

  @Column(name = "ram", nullable = false)
  private Integer ram;

  @Column(name = "rom", nullable = false)
  private Integer rom;

  @Column(name = "pin", nullable = false)
  private Integer pin;

  @Column(name = "chipset", nullable = false)
  private String chipset;

  // @PrePersist
  // protected void prePersist() {
  // StringBuilder slg = new StringBuilder(SlugUtils.slugGenerator(this));
  // super.setSlug(slg.toString());
  // super.prePersist();
  // }
  //
  // @PreUpdate
  // protected void preUpdate() {
  // StringBuilder slg = new StringBuilder(SlugUtils.slugGenerator(this));
  // super.setSlug(slg.toString());
  // super.preUpdate();
  // }
}
