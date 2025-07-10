package com.winnguyen1905.product.core.model.viewmodel;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private UUID id;
    private UUID productId;
    private UUID variantId;
    private String url;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String title;
    private String altText;
    private String description;
    private Boolean isPrimary;
    private Integer displayOrder;
    private Boolean isActive;
    private Integer width;
    private Integer height;
    private String thumbnailUrl;
    private String smallUrl;
    private String mediumUrl;
    private String largeUrl;
}
