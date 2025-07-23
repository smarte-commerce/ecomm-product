package com.winnguyen1905.product.persistance.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.UUID;

/**
 * Elasticsearch document for Inventory data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "inventory")
public class ESInventory implements Serializable {

    @Id
    private UUID id;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Integer)
    private Integer quantityAvailable;

    @Field(type = FieldType.Integer)
    private Integer quantityReserved;

    @Field(type = FieldType.Integer)
    private Integer quantitySold;

    private static final long serialVersionUID = 1L;
} 
