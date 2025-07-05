package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.winnguyen1905.product.common.constant.ProductStatus;

import lombok.Builder;

@Builder
public record VendorProductStats(
    UUID vendorId,
    String vendorName,
    
    // Overall counts
    Long totalProducts,
    Long publishedProducts,
    Long draftProducts,
    Long activeProducts,
    Long inactiveProducts,
    Long deletedProducts,
    
    // Status breakdown
    Map<ProductStatus, Long> productsByStatus,
    
    // Inventory stats
    Long totalVariants,
    Long inStockVariants,
    Long outOfStockVariants,
    Long lowStockVariants,
    Integer totalInventoryQuantity,
    Integer totalReservedQuantity,
    Integer totalAvailableQuantity,
    
    // Sales stats
    Long totalViews,
    Long totalPurchases,
    BigDecimal totalRevenue,
    BigDecimal averageProductPrice,
    BigDecimal highestProductPrice,
    BigDecimal lowestProductPrice,
    
    // Recent activity
    Long productsCreatedLastWeek,
    Long productsUpdatedLastWeek,
    Long productsPublishedLastWeek,
    
    // Performance metrics
    BigDecimal averageRating,
    Long totalReviews,
    BigDecimal conversionRate, // purchases / views
    
    // Time stamps
    Instant lastProductCreated,
    Instant lastProductUpdated,
    Instant statsGeneratedAt
) {
    
    /**
     * Calculate stock coverage percentage
     */
    public double getStockCoveragePercentage() {
        if (totalVariants == null || totalVariants == 0) {
            return 0.0;
        }
        return (inStockVariants != null ? inStockVariants.doubleValue() : 0.0) / totalVariants.doubleValue() * 100.0;
    }
    
    /**
     * Calculate publish rate percentage  
     */
    public double getPublishRatePercentage() {
        if (totalProducts == null || totalProducts == 0) {
            return 0.0;
        }
        return (publishedProducts != null ? publishedProducts.doubleValue() : 0.0) / totalProducts.doubleValue() * 100.0;
    }
    
    /**
     * Calculate average views per product
     */
    public double getAverageViewsPerProduct() {
        if (totalProducts == null || totalProducts == 0) {
            return 0.0;
        }
        return (totalViews != null ? totalViews.doubleValue() : 0.0) / totalProducts.doubleValue();
    }
    
    /**
     * Calculate average purchases per product
     */
    public double getAveragePurchasesPerProduct() {
        if (totalProducts == null || totalProducts == 0) {
            return 0.0;
        }
        return (totalPurchases != null ? totalPurchases.doubleValue() : 0.0) / totalProducts.doubleValue();
    }
} 
