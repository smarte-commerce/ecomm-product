package com.winnguyen1905.product.persistance.entity;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import com.winnguyen1905.product.common.ProductTypeConstant;

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
@Table(name = "fashions")
@DiscriminatorValue(ProductTypeConstant.FASHION)
@PrimaryKeyJoinColumn(name = "fashion_id")
public class EFashion extends EProduct {
  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  private JsonNode features;
}
