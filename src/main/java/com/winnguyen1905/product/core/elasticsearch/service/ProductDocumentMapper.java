package com.winnguyen1905.product.core.elasticsearch.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductDocumentMapper {

    /**
     * Convert EProduct and its variants to ProductDocument list
     */
    public List<ProductDocument> toProductDocuments(EProduct product) {
        if (product == null) {
            return List.of();
        }

        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            // Create a single document for the product without variants
            return List.of(toProductDocument(product, null));
        }

        // Create a document for each variant
        return product.getVariants().stream()
                .map(variant -> toProductDocument(product, variant))
                .collect(Collectors.toList());
    }

    /**
     * Convert EProduct and variant to single ProductDocument
     */
    public ProductDocument toProductDocument(EProduct product, EProductVariant variant) {
        if (product == null) {
            return null;
        }

        try {
            ProductDocument.ProductDocumentBuilder builder = ProductDocument.builder()
                    .id(variant != null ? variant.getId().toString() : product.getId().toString())
                    .productId(product.getId())
                    .variantId(variant != null ? variant.getId() : null)
                    .shopId(product.getShopId())
                    .region(product.getRegion())
                    
                    // Product information
                    .name(buildProductName(product, variant))
                    .description(product.getDescription())
                    .sku(variant != null ? variant.getSku() : null)
                    
                    // Price information
                    .price(variant != null && variant.getPrice() != null ? variant.getPrice().doubleValue() : 
                           product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0)
                    .currency("USD") // Default currency
                    
                    // Status
                    .status(product.getStatus() != null ? product.getStatus().toString() : "ACTIVE")
                    .isPublished(product.getIsPublished())
                    .isFeatured(false) // Default value
                    
                    // Category and Brand
                    .category(toCategoryDocument(product.getCategory()))
                    .brand(toBrandDocument(product.getBrand()))
                    
                    // Inventory
                    .inventory(toInventoryDocument(variant, product))
                    
                    // Images
                    .imageUrl(getPrimaryImageUrl(product, variant))
                    .imageUrls(getImageUrls(product, variant))
                    
                    // Features
                    .features(convertToFeatureMap(product.getFeatures()))
                    
                    // Analytics
                    .rating(0.0) // Default rating
                    .reviewCount(0) // Default review count
                    .viewCount(product.getViewCount() != null ? product.getViewCount().intValue() : 0)
                    .purchaseCount(product.getPurchaseCount() != null ? product.getPurchaseCount().intValue() : 0)
                    
                    // Tags (empty for now)
                    .tags(List.of())
                    
                    // SEO
                    .seoTitle(product.getMetaTitle() != null ? product.getMetaTitle() : product.getName())
                    .seoDescription(product.getMetaDescription() != null ? product.getMetaDescription() : product.getDescription())
                    .seoKeywords(parseMetaKeywords(product.getMetaKeywords()))
                    
                    // Timestamps
                    .createdDate(product.getCreatedDate())
                    .updatedDate(product.getUpdatedDate())
                    .createdBy(product.getCreatedBy())
                    .updatedBy(product.getUpdatedBy());

            return builder.build();
        } catch (Exception e) {
            log.error("Error mapping product entity to document: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert ProductDocument to ProductVariantReviewVm
     */
    public ProductVariantReviewVm toProductVariantReviewVm(ProductDocument document) {
        if (document == null) {
            return null;
        }

        try {
            return ProductVariantReviewVm.builder()
                    .id(document.getVariantId() != null ? document.getVariantId() : document.getProductId())
                    .productId(document.getProductId())
                    .name(document.getName())
                    .price(document.getPrice())
                    .imageUrl(document.getImageUrl())
                    .features(document.getFeatures())
                    .stock(document.getInventory() != null ? document.getInventory().getQuantityAvailable() : 0)
                    .sku(document.getSku())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping document to ProductVariantReviewVm: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert list of ProductDocuments to ProductVariantReviewVms
     */
    public List<ProductVariantReviewVm> toProductVariantReviewVms(List<ProductDocument> documents) {
        if (documents == null) {
            return List.of();
        }

        return documents.stream()
                .map(this::toProductVariantReviewVm)
                .collect(Collectors.toList());
    }

    // Helper methods

    private String buildProductName(EProduct product, EProductVariant variant) {
        if (variant == null || variant.getName() == null || variant.getName().trim().isEmpty()) {
            return product.getName();
        }

        // Combine product name with variant-specific information
        return product.getName() + " - " + variant.getName();
    }

    private ProductDocument.CategoryDocument toCategoryDocument(ECategory category) {
        if (category == null) {
            return null;
        }

        return ProductDocument.CategoryDocument.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .path(category.getCategoryPath())
                .level(category.getCategoryLevel())
                .leftBound(0L) // Default values since these fields may not be used
                .rightBound(0L)
                .build();
    }

    private ProductDocument.BrandDocument toBrandDocument(EBrand brand) {
        if (brand == null) {
            return null;
        }

        return ProductDocument.BrandDocument.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .website(brand.getWebsiteUrl()) // Use correct field name
                .isVerified(brand.getIsVerified())
                .build();
    }

    private ProductDocument.InventoryDocument toInventoryDocument(EProductVariant variant, EProduct product) {
        if (variant == null) {
            return null;
        }

        // Use variant's inventory quantities directly
        int quantityAvailable = variant.getInventoryQuantity() != null ? variant.getInventoryQuantity() : 0;
        int quantityReserved = variant.getReservedQuantity() != null ? variant.getReservedQuantity() : 0;
        int lowStockThreshold = product.getLowStockThreshold() != null ? product.getLowStockThreshold() : 10;

        return ProductDocument.InventoryDocument.builder()
                .id(variant.getId()) // Use variant ID as inventory ID
                .sku(variant.getSku())
                .quantityAvailable(quantityAvailable)
                .quantityReserved(quantityReserved)
                .quantitySold(0) // Default since not available from variant
                .lowStockThreshold(lowStockThreshold)
                .isInStock(quantityAvailable > 0)
                .isLowStock(quantityAvailable <= lowStockThreshold && quantityAvailable > 0)
                .lastUpdated(variant.getUpdatedDate())
                .build();
    }

    private String getPrimaryImageUrl(EProduct product, EProductVariant variant) {
        // First try to get variant-specific image
        if (variant != null && variant.getImages() != null && !variant.getImages().isEmpty()) {
            String variantImage = variant.getImages().stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                    .map(EProductImage::getUrl)
                    .findFirst()
                    .orElse(variant.getImages().get(0).getUrl());
            if (variantImage != null) {
                return variantImage;
            }
        }

        // Fall back to product-level primary image
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            return product.getImages().stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                    .map(EProductImage::getUrl)
                    .findFirst()
                    .orElse(product.getImages().get(0).getUrl());
        }

        return null;
    }

    private List<String> getImageUrls(EProduct product, EProductVariant variant) {
        List<String> imageUrls = new java.util.ArrayList<>();

        // Collect variant-specific images first
        if (variant != null && variant.getImages() != null) {
            List<String> variantImages = variant.getImages().stream()
                    .map(EProductImage::getUrl)
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .collect(Collectors.toList());
            imageUrls.addAll(variantImages);
        }

        // Add product-level images
        if (product.getImages() != null) {
            List<String> productImages = product.getImages().stream()
                    .filter(image -> image.getVariant() == null) // Only product-level images
                    .map(EProductImage::getUrl)
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .collect(Collectors.toList());
            
            imageUrls.addAll(productImages);
        }

        return imageUrls;
    }

    private Map<String, Object> convertToFeatureMap(Object features) {
        if (features instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> featureMap = (Map<String, Object>) features;
                return featureMap;
            } catch (ClassCastException e) {
                log.warn("Unable to cast features to Map<String, Object>: {}", e.getMessage());
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private List<String> parseMetaKeywords(String metaKeywords) {
        if (metaKeywords == null || metaKeywords.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(metaKeywords.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .collect(Collectors.toList());
    }
} 
