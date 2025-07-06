package com.winnguyen1905.product.core.elasticsearch.document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

import com.winnguyen1905.product.secure.RegionPartition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products", writeTypeHint = WriteTypeHint.FALSE)
public class ProductDocument {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Keyword, name = "product_id")
    private UUID productId;
    
    @Field(type = FieldType.Keyword, name = "variant_id")
    private UUID variantId;
    
    @Field(type = FieldType.Keyword, name = "shop_id")
    private UUID shopId;
    
    @Field(type = FieldType.Keyword, name = "region")
    private RegionPartition region;
    
    @Field(type = FieldType.Text, name = "name", analyzer = "product_name_analyzer")
    private String name;
    
    @Field(type = FieldType.Text, name = "description", analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword, name = "sku")
    private String sku;
    
    @Field(type = FieldType.Double, name = "price")
    private Double price;
    
    @Field(type = FieldType.Keyword, name = "currency")
    private String currency;
    
    @Field(type = FieldType.Text, name = "image_url")
    private String imageUrl;
    
    @Field(type = FieldType.Keyword, name = "image_urls")
    private List<String> imageUrls;
    
    @Field(type = FieldType.Nested, name = "category")
    private CategoryDocument category;
    
    @Field(type = FieldType.Nested, name = "brand")
    private BrandDocument brand;
    
    @Field(type = FieldType.Nested, name = "inventory")
    private InventoryDocument inventory;
    
    @Field(type = FieldType.Object, name = "features")
    private Map<String, Object> features;
    
    @Field(type = FieldType.Keyword, name = "status")
    private String status;
    
    @Field(type = FieldType.Boolean, name = "is_published")
    private Boolean isPublished;
    
    @Field(type = FieldType.Boolean, name = "is_featured")
    private Boolean isFeatured;
    
    @Field(type = FieldType.Double, name = "rating")
    private Double rating;
    
    @Field(type = FieldType.Integer, name = "review_count")
    private Integer reviewCount;
    
    @Field(type = FieldType.Integer, name = "view_count")
    private Integer viewCount;
    
    @Field(type = FieldType.Integer, name = "purchase_count")
    private Integer purchaseCount;
    
    @Field(type = FieldType.Keyword, name = "tags")
    private List<String> tags;
    
    @Field(type = FieldType.Text, name = "seo_title")
    private String seoTitle;
    
    @Field(type = FieldType.Text, name = "seo_description")
    private String seoDescription;
    
    @Field(type = FieldType.Keyword, name = "seo_keywords")
    private List<String> seoKeywords;
    
    @Field(type = FieldType.Date, name = "created_date")
    private Instant createdDate;
    
    @Field(type = FieldType.Date, name = "updated_date")
    private Instant updatedDate;
    
    @Field(type = FieldType.Keyword, name = "created_by")
    private String createdBy;
    
    @Field(type = FieldType.Keyword, name = "updated_by")
    private String updatedBy;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDocument {
        
        @Field(type = FieldType.Keyword, name = "id")
        private UUID id;
        
        @Field(type = FieldType.Text, name = "name", analyzer = "standard")
        private String name;
        
        @Field(type = FieldType.Text, name = "description")
        private String description;
        
        @Field(type = FieldType.Keyword, name = "parent_id")
        private UUID parentId;
        
        @Field(type = FieldType.Keyword, name = "path")
        private String path;
        
        @Field(type = FieldType.Integer, name = "level")
        private Integer level;
        
        @Field(type = FieldType.Long, name = "left_bound")
        private Long leftBound;
        
        @Field(type = FieldType.Long, name = "right_bound")
        private Long rightBound;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandDocument {
        
        @Field(type = FieldType.Keyword, name = "id")
        private UUID id;
        
        @Field(type = FieldType.Text, name = "name", analyzer = "standard")
        private String name;
        
        @Field(type = FieldType.Text, name = "description")
        private String description;
        
        @Field(type = FieldType.Keyword, name = "logo_url")
        private String logoUrl;
        
        @Field(type = FieldType.Keyword, name = "website")
        private String website;
        
        @Field(type = FieldType.Boolean, name = "is_verified")
        private Boolean isVerified;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryDocument {
        
        @Field(type = FieldType.Keyword, name = "id")
        private UUID id;
        
        @Field(type = FieldType.Keyword, name = "sku")
        private String sku;
        
        @Field(type = FieldType.Integer, name = "quantity_available")
        private Integer quantityAvailable;
        
        @Field(type = FieldType.Integer, name = "quantity_reserved")
        private Integer quantityReserved;
        
        @Field(type = FieldType.Integer, name = "quantity_sold")
        private Integer quantitySold;
        
        @Field(type = FieldType.Integer, name = "low_stock_threshold")
        private Integer lowStockThreshold;
        
        @Field(type = FieldType.Boolean, name = "is_in_stock")
        private Boolean isInStock;
        
        @Field(type = FieldType.Boolean, name = "is_low_stock")
        private Boolean isLowStock;
        
        @Field(type = FieldType.Date, name = "last_updated")
        private Instant lastUpdated;
    }
} 
