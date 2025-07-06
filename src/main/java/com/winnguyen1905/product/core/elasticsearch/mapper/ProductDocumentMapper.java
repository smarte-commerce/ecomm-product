package com.winnguyen1905.product.core.elasticsearch.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductDocumentMapper {

    public ProductDocument fromProductEntity(EProduct product, EProductVariant variant) {
        if (product == null || variant == null) {
            log.warn("Product or variant is null, cannot create ProductDocument");
            return null;
        }

        try {
            return ProductDocument.builder()
                .id(variant.getId().toString())
                .productId(product.getId())
                .variantId(variant.getId())
                .shopId(product.getShopId())
                .region(variant.getRegion())
                .name(variant.getName())
                .description(variant.getDescription())
                .sku(variant.getSku())
                .price(variant.getPrice() != null ? variant.getPrice().doubleValue() : 0.0)
                .currency("USD") // Default currency
                .imageUrl(getImageUrl(variant))
                .imageUrls(getImageUrls(product))
                .category(mapCategory(product.getCategory()))
                .brand(mapBrand(product.getBrand()))
                .inventory(mapInventory(product, variant))
                .features(variant.getFeatures() instanceof Map ? (Map<String,Object>) variant.getFeatures() : new HashMap<>())
                .status(product.getStatus() != null ? product.getStatus().toString() : "ACTIVE")
                .isPublished(product.getIsPublished())
                .isFeatured(false) // Default to false since getIsFeatured doesn't exist
                .rating(0.0) // Default rating
                .reviewCount(0) // Default review count
                .viewCount(0) // Default view count
                .purchaseCount(0) // Default purchase count
                .tags(new ArrayList<>()) // Default empty tags
                .seoTitle(product.getName())
                .seoDescription(product.getDescription())
                .seoKeywords(new ArrayList<>()) // Default empty SEO keywords
                .createdDate(product.getCreatedDate())
                .updatedDate(product.getUpdatedDate())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
        } catch (Exception e) {
            log.error("Error mapping product entity to document: {}", e.getMessage(), e);
            return null;
        }
    }

    public ProductVariantReviewVm toProductVariantReviewVm(ProductDocument document) {
        if (document == null) {
            log.warn("ProductDocument is null, cannot create ProductVariantReviewVm");
            return null;
        }

        try {
            return ProductVariantReviewVm.builder()
                .id(document.getVariantId())
                .name(document.getName())
                .imageUrl(document.getImageUrl())
                .price(document.getPrice() != null ? document.getPrice().doubleValue() : 0.0)
                .stock(document.getInventory() != null ? document.getInventory().getQuantityAvailable() : 0)
                .productId(document.getProductId())
                .features(document.getFeatures())
                .sku(document.getSku())
                .build();
        } catch (Exception e) {
            log.error("Error mapping document to ProductVariantReviewVm: {}", e.getMessage(), e);
            return null;
        }
    }

    private ProductDocument.CategoryDocument mapCategory(ECategory category) {
        if (category == null) {
            return null;
        }

        return ProductDocument.CategoryDocument.builder()
            .id(category.getId())
            .name(category.getName())
            .description(category.getDescription())
            .parentId(category.getParent() != null ? category.getParent().getId() : null)
            .path(buildCategoryPath(category))
            .level(calculateCategoryLevel(category))
            .leftBound(0L) // Default since getLeft() doesn't exist
            .rightBound(0L) // Default since getRight() doesn't exist
            .build();
    }

    private ProductDocument.BrandDocument mapBrand(EBrand brand) {
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

    private ProductDocument.InventoryDocument mapInventory(EProduct product, EProductVariant variant) {
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
            .quantitySold(0) // Default since not available
            .lowStockThreshold(lowStockThreshold)
            .isInStock(quantityAvailable > 0)
            .isLowStock(quantityAvailable <= lowStockThreshold && quantityAvailable > 0)
            .lastUpdated(variant.getUpdatedDate())
            .build();
    }

    private String getImageUrl(EProductVariant variant) {
        // Try to get from variant images first
        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
            return variant.getImages().get(0).getUrl();
        }
        
        // Try to get from product images
        if (variant.getProduct() != null && variant.getProduct().getImages() != null && !variant.getProduct().getImages().isEmpty()) {
            return variant.getProduct().getImages().get(0).getUrl();
        }
        
        return null;
    }

    private List<String> getImageUrls(EProduct product) {
        if (product == null || product.getImages() == null) {
            return new ArrayList<>();
        }
        
        return product.getImages().stream()
            .map(image -> image.getUrl())
            .filter(url -> url != null && !url.isEmpty())
            .toList();
    }

    private String buildCategoryPath(ECategory category) {
        if (category == null) {
            return "";
        }
        
        // Use the categoryPath field if available
        if (category.getCategoryPath() != null) {
            return category.getCategoryPath();
        }
        
        // Otherwise just return the category name
        return category.getName();
    }

    private Integer calculateCategoryLevel(ECategory category) {
        if (category == null) {
            return 0;
        }
        
        // Use the categoryLevel field if available
        if (category.getCategoryLevel() != null) {
            return category.getCategoryLevel();
        }
        
        // Otherwise calculate based on parent
        return category.getParent() != null ? 1 : 0;
    }
} 
