package com.winnguyen1905.product.persistance.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import com.winnguyen1905.product.common.ProductTypeConstant;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "accessories")
@PrimaryKeyJoinColumn(name = "accessory_id")
@DiscriminatorValue(ProductTypeConstant.ACCESSORY)
public class EAccessory extends EProduct {
  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  private JsonNode features;
}
