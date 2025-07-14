package com.winnguyen1905.product.persistance.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.product.secure.RegionPartition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Elasticsearch document for Product Variant data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "product_variants")
public class ESProductVariant implements Serializable {

    @Id
    private UUID id;

    @Field(type = FieldType.Keyword)
    private UUID productId;

    @Field(type = FieldType.Keyword)
    private RegionPartition region;

    @Field(type = FieldType.Object)
    private JsonNode features;

    @Field(type = FieldType.Text)
    private String imageUrl;

    @Field(type = FieldType.Text)
    private String brand;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Object)
    private ESInventory inventory;

    @Field(type = FieldType.Keyword)
    private String createdBy;

    @Field(type = FieldType.Keyword)
    private String updatedBy;

    @Field(type = FieldType.Date)
    private Instant createdDate;

    @Field(type = FieldType.Date)
    private Instant updatedDate;

    private static final long serialVersionUID = 1L;
} 
