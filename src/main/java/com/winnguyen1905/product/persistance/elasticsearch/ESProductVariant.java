package com.winnguyen1905.product.persistance.elasticsearch;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.product.persistance.entity.EBase;
import com.winnguyen1905.product.persistance.entity.EBaseAudit;

@Getter
@Setter
@SuperBuilder
@Document(indexName = "productv1", writeTypeHint = WriteTypeHint.FALSE, storeIdInSource = true)
public class ESProductVariant extends EBase {

  @Field(type = FieldType.Integer, name = "product_id")
  private UUID productId;

  @Field(type = FieldType.Text, name = "name")
  private String name;

  @Field(type = FieldType.Text, name = "description")
  private String description;

  @Field(type = FieldType.Keyword, name = "brand")
  private String brand;

  @Field(type = FieldType.Keyword, name = "brand_category")
  private String brandCategory;

  @Field(type = FieldType.Double_Range, name = "price")
  private double price;

  @Field(type = FieldType.Object, name = "category")
  private ESCategory category;

  @Field(type = FieldType.Object, name = "feature")
  private Object features;

  @Field(type = FieldType.Object, name = "inventory")
  private ESInventory inventory;

  @Field(type = FieldType.Date, name = "created_by")
  private String createdBy;

  @Field(type = FieldType.Date, name = "updated_by")
  private String updatedBy;

  @Field(type = FieldType.Date, name = "created_date")
  private Instant createdDate;

  @Field(type = FieldType.Date, name = "updated_date")
  private Instant updatedDate;

}
