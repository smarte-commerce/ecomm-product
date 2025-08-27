package com.winnguyen1905.product.core.mapper_v2;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.model.request.CreateProductImageRequest;
import com.winnguyen1905.product.core.model.request.CreateProductRequest;
import com.winnguyen1905.product.core.model.request.CreateProductVariantRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.response.ProductVariantResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;

/**
 * Enhanced Product Mapper
 * Comprehensive mapping between entities and DTOs with support for complex business logic
 */
public class EnhancedProductMapper {

    // ================== PRODUCT MAPPING ==================

    /**
     * Map CreateProductRequest to EProduct entity
     */
    public static EProduct toEntity(CreateProductRequest request) {
        if (request == null) return null;

        return EProduct.builder()
                .name(request.name())
                .description(request.description())
                .shortDescription(request.shortDescription())
                .slug(request.slug())
                .productType(request.productType())
                .vendorId(request.vendorId())
                .shopId(request.shopId())
                .region(request.region())
                .basePrice(request.basePrice())
                .features(request.features())
                .specifications(request.specifications())
                .weight(request.weight())
                .length(request.length())
                .width(request.width())
                .height(request.height())
                .trackInventory(request.trackInventory())
                .allowBackorder(request.allowBackorder())
                .lowStockThreshold(request.lowStockThreshold())
                .requiresShipping(request.requiresShipping())
                .metaTitle(request.metaTitle())
                .metaDescription(request.metaDescription())
                .metaKeywords(request.metaKeywords())
                .tags(request.tags())
                .status(ProductStatus.DRAFT)
                .isPublished(false)
                .isDeleted(false)
                .viewCount(0L)
                .purchaseCount(0L)
                .ratingCount(0)
                .build();
    }

    /**
     * Map EProduct entity to ProductResponse DTO
     */
    public static ProductResponse toResponse(EProduct entity) {
        if (entity == null) return null;

        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .shortDescription(entity.getShortDescription())
                .slug(entity.getSlug())
                .productType(entity.getProductType())
                .status(entity.getStatus())
                .isPublished(entity.getIsPublished())
                .vendorId(entity.getVendorId())
                .shopId(entity.getShopId())
                .region(entity.getRegion())
                .brand(mapBrandInfo(entity.getBrand()))
                .category(mapCategoryInfo(entity.getCategory()))
                .minPrice(entity.getMinPrice())
                .maxPrice(entity.getMaxPrice())
                .basePrice(entity.getBasePrice())
                .weight(entity.getWeight())
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .features(entity.getFeatures())
                .specifications(entity.getSpecifications())
                .metaTitle(entity.getMetaTitle())
                .metaDescription(entity.getMetaDescription())
                .metaKeywords(entity.getMetaKeywords())
                .tags(entity.getTags())
                .trackInventory(entity.getTrackInventory())
                .allowBackorder(entity.getAllowBackorder())
                .lowStockThreshold(entity.getLowStockThreshold())
                .requiresShipping(entity.getRequiresShipping())
                .viewCount(entity.getViewCount())
                .purchaseCount(entity.getPurchaseCount())
                .ratingAverage(entity.getRatingAverage())
                .ratingCount(entity.getRatingCount())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .variants(mapVariantResponses(entity.getVariants()))
                .images(mapImageResponses(entity.getImages()))
                .inventory(mapInventorySummary(entity.getInventories()))
                .build();
    }

    /**
     * Update EProduct entity from UpdateProductRequest
     */
    public static void updateEntityFromRequest(EProduct entity, UpdateProductRequest request) {
        if (entity == null || request == null) return;

        Optional.ofNullable(request.name()).ifPresent(entity::setName);
        Optional.ofNullable(request.description()).ifPresent(entity::setDescription);
        Optional.ofNullable(request.slug()).ifPresent(entity::setSlug);
        Optional.ofNullable(request.features()).ifPresent(entity::setFeatures);
        Optional.ofNullable(request.isPublished()).ifPresent(entity::setIsPublished);
    }

    // ================== VARIANT MAPPING ==================

    /**
     * Map CreateProductVariantRequest to EProductVariant entity
     */
    public static EProductVariant toVariantEntity(CreateProductVariantRequest request, EProduct product) {
        if (request == null) return null;

        return EProductVariant.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price() != null ? request.price().doubleValue() : null)
                .compareAtPrice(request.compareAtPrice())
                .costPrice(request.costPrice())
                .weight(request.weight())
                .length(request.length())
                .width(request.width())
                .height(request.height())
                .attributes(request.attributes())
                .features(request.features())
                .trackInventory(request.trackInventory())
                .inventoryQuantity(request.inventoryQuantity())
                .isDefault(request.isDefault())
                .isActive(request.isActive())
                .product(product)
                .vendorId(product != null ? product.getVendorId() : null)
                .region(product != null ? product.getRegion() : null)
                .isDeleted(false)
                .reservedQuantity(0)
                .viewCount(0L)
                .purchaseCount(0L)
                .build();
    }

    /**
     * Map EProductVariant entity to ProductVariantResponse DTO
     */
    public static ProductVariantResponse toVariantResponse(EProductVariant entity) {
        if (entity == null) return null;

        return ProductVariantResponse.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice() != null ? BigDecimal.valueOf(entity.getPrice()) : null)
                .compareAtPrice(entity.getCompareAtPrice())
                .costPrice(entity.getCostPrice())
                .isActive(entity.getIsActive())
                .isDefault(entity.getIsDefault())
                .weight(entity.getWeight())
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .attributes(entity.getAttributes())
                .features(entity.getFeatures())
                .trackInventory(entity.getTrackInventory())
                .inventoryQuantity(entity.getInventoryQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .availableQuantity(entity.getAvailableQuantity())
                .viewCount(entity.getViewCount())
                .purchaseCount(entity.getPurchaseCount())
                .vendorId(entity.getVendorId())
                .region(entity.getRegion())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .images(mapImageResponses(entity.getImages()))
                .hasDiscount(entity.hasDiscount())
                .discountPercentage(entity.getDiscountPercentage())
                .profitMargin(entity.getProfitMargin())
                .isAvailable(entity.isAvailable())
                .inStock(entity.getAvailableQuantity() > 0)
                .build();
    }

    // ================== IMAGE MAPPING ==================

    /**
     * Map CreateProductImageRequest to EProductImage entity
     */
    public static EProductImage toImageEntity(CreateProductImageRequest request, EProduct product, EProductVariant variant) {
        if (request == null) return null;

        return EProductImage.builder()
                .url(request.getUrl())
                .altText(request.getAltText())
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .displayOrder(request.getDisplayOrder())
                .isPrimary(request.getIsPrimary())
                .isActive(request.getIsActive())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .mimeType(request.getMimeType())
                .width(request.getWidth())
                .height(request.getHeight())
                .cdnUrl(request.getCdnUrl())
                .thumbnailUrl(request.getThumbnailUrl())
                .smallUrl(request.getSmallUrl())
                .mediumUrl(request.getMediumUrl())
                .largeUrl(request.getLargeUrl())
                .storageProvider(request.getStorageProvider())
                .storageBucket(request.getStorageBucket())
                .storageKey(request.getStorageKey())
                .colorPalette(request.getColorPalette())
                .isOptimized(request.getIsOptimized())
                .compressionQuality(request.getCompressionQuality())
                .product(product)
                .variant(variant)
                .vendorId(product != null ? product.getVendorId() : null)
                .isDeleted(false)
                .viewCount(0L)
                .clickCount(0L)
                .build();
    }

    /**
     * Map EProductImage to ProductImageResponse
     */
    private static ProductImageResponse mapImageResponse(EProductImage image) {
        if (image == null) return null;

        ProductImageResponse response = new ProductImageResponse();
        response.setId(image.getId());
        response.setUrl(image.getUrl());
        response.setProductId(image.getProduct() != null ? image.getProduct().getId() : null);
        response.setVariantId(image.getVariant() != null ? image.getVariant().getId() : null);
        response.setTitle(image.getTitle());
        response.setAltText(image.getAltText());
        response.setDescription(image.getDescription());
        response.setFileName(image.getFileName());
        response.setFileSize(image.getFileSize());
        response.setMimeType(image.getMimeType());
        response.setWidth(image.getWidth());
        response.setHeight(image.getHeight());
        response.setDisplayOrder(image.getDisplayOrder());
        response.setIsPrimary(image.getIsPrimary());
        response.setIsActive(image.getIsActive());
        response.setThumbnailUrl(image.getThumbnailUrl());
        response.setSmallUrl(image.getSmallUrl());
        response.setMediumUrl(image.getMediumUrl());
        response.setLargeUrl(image.getLargeUrl());
        return response;
    }

    // ================== HELPER METHODS ==================

    /**
     * Map Brand to BrandInfo
     */
    private static ProductResponse.BrandInfo mapBrandInfo(EBrand brand) {
        if (brand == null) return null;

        return ProductResponse.BrandInfo.builder()
                .id(brand.getId())
                .name(brand.getName())
                .code(brand.getCode())
                .logoUrl(brand.getLogoUrl())
                .isVerified(brand.getIsVerified())
                .build();
    }

    /**
     * Map Category to CategoryInfo
     */
    private static ProductResponse.CategoryInfo mapCategoryInfo(ECategory category) {
        if (category == null) return null;

        return ProductResponse.CategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .categoryPath(category.getCategoryPath())
                .categoryLevel(category.getCategoryLevel())
                .build();
    }

    /**
     * Map variants to response list
     */
    private static List<ProductVariantResponse> mapVariantResponses(List<EProductVariant> variants) {
        if (variants == null) return List.of();

        return variants.stream()
                .filter(variant -> variant.getIsDeleted() == null || !variant.getIsDeleted())
                .map(EnhancedProductMapper::toVariantResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map images to response list
     */
    private static List<ProductImageResponse> mapImageResponses(List<EProductImage> images) {
        if (images == null) return List.of();

        return images.stream()
                .filter(image -> image.getIsDeleted() == null || !image.getIsDeleted())
                .map(EnhancedProductMapper::mapImageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map inventories to summary
     */
    private static ProductResponse.InventorySummary mapInventorySummary(List<EInventory> inventories) {
        if (inventories == null || inventories.isEmpty()) {
            return ProductResponse.InventorySummary.builder()
                    .totalQuantity(0)
                    .availableQuantity(0)
                    .reservedQuantity(0)
                    .soldQuantity(0)
                    .inStock(false)
                    .build();
        }

        int totalQuantity = inventories.stream()
                .mapToInt(inv -> inv.getQuantityAvailable() != null ? inv.getQuantityAvailable() : 0)
                .sum();

        int reservedQuantity = inventories.stream()
                .mapToInt(inv -> inv.getQuantityReserved() != null ? inv.getQuantityReserved() : 0)
                .sum();

        int soldQuantity = inventories.stream()
                .mapToInt(inv -> inv.getQuantitySold() != null ? inv.getQuantitySold() : 0)
                .sum();

        int availableQuantity = totalQuantity - reservedQuantity;

        return ProductResponse.InventorySummary.builder()
                .totalQuantity(totalQuantity)
                .availableQuantity(Math.max(0, availableQuantity))
                .reservedQuantity(reservedQuantity)
                .soldQuantity(soldQuantity)
                .inStock(availableQuantity > 0)
                .build();
    }

    // ================== BATCH MAPPING ==================

    /**
     * Map list of entities to response list
     */
    public static List<ProductResponse> toResponseList(List<EProduct> entities) {
        if (entities == null) return List.of();

        return entities.stream()
                .map(EnhancedProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map list of variant entities to response list
     */
    public static List<ProductVariantResponse> toVariantResponseList(List<EProductVariant> entities) {
        if (entities == null) return List.of();

        return entities.stream()
                .map(EnhancedProductMapper::toVariantResponse)
                .collect(Collectors.toList());
    }

    // ================== UTILITY METHODS ==================

    /**
     * Generate SEO-friendly slug
     */
    public static String generateSlug(String name, UUID vendorId) {
        if (name == null || name.trim().isEmpty()) {
            return "product-" + System.currentTimeMillis();
        }

        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return baseSlug + "-" + vendorId.toString().substring(0, 8);
    }

    /**
     * Calculate price range for product with variants
     */
    public static void updateProductPriceRange(EProduct product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return;
        }

        List<Double> prices = product.getVariants().stream()
                .filter(v -> v.getPrice() != null && (v.getIsDeleted() == null || !v.getIsDeleted()))
                .map(EProductVariant::getPrice)
                .collect(Collectors.toList());

        if (!prices.isEmpty()) {
            BigDecimal minPrice = prices.stream()
                    .map(BigDecimal::valueOf)
                    .min(BigDecimal::compareTo)
                    .orElse(product.getBasePrice());
            BigDecimal maxPrice = prices.stream()
                    .map(BigDecimal::valueOf)
                    .max(BigDecimal::compareTo)
                    .orElse(product.getBasePrice());
                    
            product.setMinPrice(minPrice);
            product.setMaxPrice(maxPrice);
        } else if (product.getBasePrice() != null) {
            product.setMinPrice(product.getBasePrice());
            product.setMaxPrice(product.getBasePrice());
        }
    }
} 
