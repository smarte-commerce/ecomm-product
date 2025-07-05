package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Product Performance Response DTO
 * 
 * Detailed product performance analytics for vendors
 */
@Builder
public record VendorProductPerformanceResponse(
    UUID vendorId,
    String vendorName,
    LocalDate reportStartDate,
    LocalDate reportEndDate,
    String sortBy, // REVENUE, SALES_COUNT, VIEWS, RATING, PROFIT_MARGIN
    Integer limit,
    
    // Overall Performance Summary
    PerformanceSummary performanceSummary,
    
    // Top Performing Products
    List<ProductPerformance> topProducts,
    
    // Category Performance
    List<CategoryPerformance> categoryPerformance,
    
    // Performance Trends
    List<PerformanceTrend> performanceTrends,
    
    // Underperforming Products
    List<ProductPerformance> underperformingProducts,
    
    // Recommendations
    List<PerformanceRecommendation> recommendations,
    
    Instant generatedAt
    
) {
    
    @Builder
    public record PerformanceSummary(
        Long totalProducts,
        Long activeProducts,
        BigDecimal totalRevenue,
        Long totalSales,
        Long totalViews,
        BigDecimal averageConversionRate,
        BigDecimal averageRating,
        BigDecimal averageProfitMargin,
        
        // Growth metrics
        BigDecimal revenueGrowth,
        BigDecimal salesGrowth,
        BigDecimal viewGrowth,
        
        // Top performers count
        Long topPerformersCount,
        Long underperformersCount
    ) {}
    
    @Builder
    public record ProductPerformance(
        UUID productId,
        String productName,
        String sku,
        String category,
        String status,
        
        // Sales metrics
        BigDecimal revenue,
        Long salesCount,
        Long unitsInStock,
        BigDecimal averageOrderValue,
        
        // Engagement metrics
        Long viewCount,
        Long wishlistCount,
        Long cartAdditions,
        BigDecimal conversionRate,
        
        // Quality metrics
        BigDecimal rating,
        Integer reviewCount,
        BigDecimal returnRate,
        
        // Financial metrics
        BigDecimal costPrice,
        BigDecimal sellingPrice,
        BigDecimal profitMargin,
        BigDecimal totalProfit,
        
        // Performance indicators
        String performanceRank, // TOP, GOOD, AVERAGE, POOR
        BigDecimal performanceScore, // 0-100
        
        // Trends
        BigDecimal revenueGrowth,
        BigDecimal salesGrowth,
        BigDecimal viewGrowth,
        
        // Inventory metrics
        Integer daysInStock,
        BigDecimal inventoryTurnover,
        Boolean isLowStock,
        Boolean isOutOfStock,
        
        // Images and details
        String primaryImageUrl,
        Instant createdDate,
        Instant lastSaleDate,
        
        // Recommendations
        List<String> improvementSuggestions
    ) {}
    
    @Builder
    public record CategoryPerformance(
        String categoryId,
        String categoryName,
        Long productCount,
        BigDecimal totalRevenue,
        Long totalSales,
        BigDecimal averageRating,
        BigDecimal profitMargin,
        BigDecimal marketShare, // Within vendor's portfolio
        
        // Growth metrics
        BigDecimal revenueGrowth,
        BigDecimal salesGrowth,
        
        // Top product in category
        ProductSummary topProduct,
        
        // Performance ranking
        Integer categoryRank,
        String performanceLevel // EXCELLENT, GOOD, AVERAGE, POOR
    ) {}
    
    @Builder
    public record ProductSummary(
        UUID productId,
        String productName,
        String sku,
        BigDecimal revenue,
        Long salesCount
    ) {}
    
    @Builder
    public record PerformanceTrend(
        LocalDate date,
        String period, // For grouped data
        BigDecimal totalRevenue,
        Long totalSales,
        Long totalViews,
        BigDecimal conversionRate,
        BigDecimal averageOrderValue,
        
        // Top product for the period
        ProductSummary topProduct
    ) {}
    
    @Builder
    public record PerformanceRecommendation(
        String type, // PRICING, INVENTORY, MARKETING, PRODUCT_IMPROVEMENT
        String title,
        String description,
        String priority, // HIGH, MEDIUM, LOW
        List<String> actionItems,
        BigDecimal potentialImpact, // Estimated revenue/sales impact
        
        // Related products
        List<UUID> relatedProductIds,
        
        // Implementation details
        String implementationEffort, // LOW, MEDIUM, HIGH
        Integer estimatedTimeToImplement // In days
    ) {}
}
