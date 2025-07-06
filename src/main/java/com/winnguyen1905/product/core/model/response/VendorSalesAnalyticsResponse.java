package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Sales Analytics Response DTO
 * 
 * Detailed sales analytics and reporting data for vendors
 */
@Builder
public record VendorSalesAnalyticsResponse(
    UUID vendorId,
    String vendorName,
    LocalDate startDate,
    LocalDate endDate,
    String groupBy, // DAY, WEEK, MONTH, QUARTER, YEAR
    
    // Overall Sales Summary
    SalesSummary salesSummary,
    
    // Time-based Sales Data
    List<SalesDataPoint> salesData,
    
    // Product Category Analysis
    List<CategorySales> salesByCategory,
    
    // Geographic Sales Distribution
    List<RegionSales> salesByRegion,
    
    // Payment Method Analysis
    List<PaymentMethodSales> salesByPaymentMethod,
    
    // Customer Segment Analysis
    CustomerSegmentAnalysis customerSegmentAnalysis,
    
    // Seasonal Trends
    SeasonalTrends seasonalTrends,
    
    // Comparison with Previous Period
    PeriodComparison periodComparison,
    
    // Top Performing Items
    TopPerformers topPerformers,
    
    java.time.Instant generatedAt
    
) {
    
    @Builder
    public record SalesSummary(
        BigDecimal totalRevenue,
        BigDecimal netRevenue, // After refunds and fees
        BigDecimal grossProfit,
        BigDecimal profitMargin,
        Long totalOrders,
        Long totalUnits,
        BigDecimal averageOrderValue,
        BigDecimal averageUnitPrice,
        BigDecimal refundAmount,
        BigDecimal refundRate,
        Long uniqueCustomers,
        BigDecimal revenuePerCustomer
    ) {}
    
    @Builder
    public record SalesDataPoint(
        LocalDate date,
        String period, // For grouped data (e.g., "2024-W01" for weekly)
        BigDecimal revenue,
        BigDecimal netRevenue,
        Long orderCount,
        Long unitsSold,
        BigDecimal averageOrderValue,
        Long uniqueCustomers,
        BigDecimal conversionRate
    ) {}
    
    @Builder
    public record CategorySales(
        String categoryId,
        String categoryName,
        BigDecimal revenue,
        BigDecimal revenuePercentage,
        Long orderCount,
        Long unitsSold,
        BigDecimal averageOrderValue,
        BigDecimal profitMargin,
        BigDecimal growthRate // Compared to previous period
    ) {}
    
    @Builder
    public record RegionSales(
        String regionCode,
        String regionName,
        BigDecimal revenue,
        BigDecimal revenuePercentage,
        Long orderCount,
        Long customerCount,
        BigDecimal averageOrderValue,
        BigDecimal marketPenetration
    ) {}
    
    @Builder
    public record PaymentMethodSales(
        String paymentMethod,
        BigDecimal revenue,
        BigDecimal revenuePercentage,
        Long transactionCount,
        BigDecimal averageTransactionValue,
        BigDecimal successRate
    ) {}
    
    @Builder
    public record CustomerSegmentAnalysis(
        NewCustomerSales newCustomers,
        ReturningCustomerSales returningCustomers,
        VIPCustomerSales vipCustomers,
        CustomerLifetimeValue customerLifetimeValue
    ) {}
    
    @Builder
    public record NewCustomerSales(
        Long count,
        BigDecimal revenue,
        BigDecimal revenuePercentage,
        BigDecimal averageOrderValue,
        BigDecimal acquisitionCost
    ) {}
    
    @Builder
    public record ReturningCustomerSales(
        Long count,
        BigDecimal revenue,
        BigDecimal revenuePercentage,
        BigDecimal averageOrderValue,
        BigDecimal retentionRate
    ) {}
    
    @Builder
    public record VIPCustomerSales(
        Long count,
        BigDecimal revenue,
        BigDecimal revenuePercentage,
        BigDecimal averageOrderValue,
        BigDecimal loyaltyScore
    ) {}
    
    @Builder
    public record CustomerLifetimeValue(
        BigDecimal averageCLV,
        BigDecimal medianCLV,
        BigDecimal totalCLV,
        Integer averageCustomerLifespan // In days
    ) {}
    
    @Builder
    public record SeasonalTrends(
        Map<String, BigDecimal> monthlyTrends, // Month name -> revenue
        Map<String, BigDecimal> quarterlyTrends,
        Map<String, BigDecimal> weeklyTrends, // Day of week -> revenue
        List<SeasonalPeak> seasonalPeaks
    ) {}
    
    @Builder
    public record SeasonalPeak(
        String period,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal peakRevenue,
        BigDecimal growthRate,
        String description
    ) {}
    
    @Builder
    public record PeriodComparison(
        LocalDate previousPeriodStart,
        LocalDate previousPeriodEnd,
        BigDecimal revenueChange,
        BigDecimal revenueChangePercentage,
        Long orderCountChange,
        BigDecimal averageOrderValueChange,
        Long customerCountChange,
        String trendDirection // UP, DOWN, STABLE
    ) {}
    
    @Builder
    public record TopPerformers(
        List<TopProduct> topProducts,
        List<TopCategory> topCategories,
        List<TopCustomer> topCustomers,
        List<TopRegion> topRegions
    ) {}
    
    @Builder
    public record TopProduct(
        UUID productId,
        String productName,
        String sku,
        BigDecimal revenue,
        Long unitsSold,
        BigDecimal profitMargin,
        BigDecimal growthRate
    ) {}
    
    @Builder
    public record TopCategory(
        String categoryId,
        String categoryName,
        BigDecimal revenue,
        Long productCount,
        BigDecimal averageProductRevenue
    ) {}
    
    @Builder
    public record TopCustomer(
        UUID customerId,
        String customerName,
        BigDecimal totalSpent,
        Long orderCount,
        BigDecimal averageOrderValue
    ) {}
    
    @Builder
    public record TopRegion(
        String regionCode,
        String regionName,
        BigDecimal revenue,
        Long customerCount,
        BigDecimal marketShare
    ) {}
}
