package com.winnguyen1905.product.persistance.elasticsearch;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import com.fasterxml.jackson.databind.JsonNode;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;

@Getter
@Setter
@Builder
@Document(indexName = "products", writeTypeHint = WriteTypeHint.FALSE, storeIdInSource = true)
public class ESProductVariant {
  @Id
  private UUID id;

  @Field(type = FieldType.Keyword, name = "product_id")
  private UUID productId;

  @Field(type = FieldType.Keyword, name = "shop_id")
  private UUID shopId;

  @Field(type = FieldType.Text, name = "name")
  private String name;

  @Field(type = FieldType.Text, name = "image_url")
  private String imageUrl;

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

  @Type(JsonType.class)
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
