package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Dashboard Response DTO
 * 
 * Comprehensive dashboard data for vendor analytics and overview
 */
@Builder
public record VendorDashboardResponse(
    UUID vendorId,
    String vendorName,
    LocalDate reportStartDate,
    LocalDate reportEndDate,
    Integer reportPeriodDays,
    
    // Key Performance Indicators
    KPIMetrics kpiMetrics,
    
    // Sales Overview
    SalesOverview salesOverview,
    
    // Product Performance
    ProductPerformance productPerformance,
    
    // Order Management
    OrderManagement orderManagement,
    
    // Financial Summary
    FinancialSummary financialSummary,
    
    // Customer Insights
    CustomerInsights customerInsights,
    
    // Inventory Status
    InventoryStatus inventoryStatus,
    
    // Recent Activities
    List<RecentActivity> recentActivities,
    
    // Alerts and Notifications
    List<Alert> alerts,
    
    // Performance Trends
    PerformanceTrends performanceTrends,
    
    Instant generatedAt
    
) {
    
    @Builder
    public record KPIMetrics(
        BigDecimal totalRevenue,
        BigDecimal revenueGrowth, // Percentage change from previous period
        Long totalOrders,
        Long orderGrowth,
        BigDecimal averageOrderValue,
        BigDecimal aovGrowth,
        BigDecimal conversionRate,
        BigDecimal conversionRateChange,
        Long totalCustomers,
        Long newCustomers,
        BigDecimal customerRetentionRate
    ) {}
    
    @Builder
    public record SalesOverview(
        BigDecimal totalSales,
        BigDecimal netSales, // After refunds and returns
        BigDecimal grossProfit,
        BigDecimal profitMargin,
        Long totalTransactions,
        Long successfulTransactions,
        Long refundedTransactions,
        BigDecimal refundRate,
        Map<String, BigDecimal> salesByCategory,
        Map<LocalDate, BigDecimal> dailySales
    ) {}
    
    @Builder
    public record ProductPerformance(
        Long totalProducts,
        Long activeProducts,
        Long draftProducts,
        Long outOfStockProducts,
        Long lowStockProducts,
        List<TopProduct> topSellingProducts,
        List<TopProduct> topViewedProducts,
        List<TopProduct> lowPerformingProducts,
        BigDecimal averageProductRating,
        Long totalProductViews,
        BigDecimal viewToSaleConversion
    ) {}
    
    @Builder
    public record TopProduct(
        UUID productId,
        String productName,
        String productSku,
        Long salesCount,
        BigDecimal revenue,
        Long viewCount,
        BigDecimal rating,
        String imageUrl
    ) {}
    
    @Builder
    public record OrderManagement(
        Long totalOrders,
        Long pendingOrders,
        Long processingOrders,
        Long shippedOrders,
        Long deliveredOrders,
        Long cancelledOrders,
        Long returnedOrders,
        BigDecimal orderFulfillmentRate,
        BigDecimal averageProcessingTime, // In hours
        BigDecimal onTimeDeliveryRate,
        List<OrderStatusCount> ordersByStatus
    ) {}
    
    @Builder
    public record OrderStatusCount(
        String status,
        Long count,
        BigDecimal percentage
    ) {}
    
    @Builder
    public record FinancialSummary(
        BigDecimal totalEarnings,
        BigDecimal pendingPayouts,
        BigDecimal availableBalance,
        BigDecimal totalFees,
        BigDecimal platformFees,
        BigDecimal paymentProcessingFees,
        BigDecimal netEarnings,
        LocalDate nextPayoutDate,
        BigDecimal nextPayoutAmount,
        List<RecentTransaction> recentTransactions
    ) {}
    
    @Builder
    public record RecentTransaction(
        UUID transactionId,
        String type, // SALE, REFUND, PAYOUT, FEE
        BigDecimal amount,
        String description,
        Instant timestamp,
        String status
    ) {}
    
    @Builder
    public record CustomerInsights(
        Long totalCustomers,
        Long newCustomers,
        Long returningCustomers,
        BigDecimal customerRetentionRate,
        BigDecimal averageCustomerLifetimeValue,
        Map<String, Long> customersByRegion,
        List<TopCustomer> topCustomers,
        BigDecimal averageOrdersPerCustomer
    ) {}
    
    @Builder
    public record TopCustomer(
        UUID customerId,
        String customerName,
        Long totalOrders,
        BigDecimal totalSpent,
        Instant lastOrderDate
    ) {}
    
    @Builder
    public record InventoryStatus(
        Long totalVariants,
        Long inStockVariants,
        Long outOfStockVariants,
        Long lowStockVariants,
        Integer totalInventoryValue,
        Integer averageInventoryTurnover,
        List<LowStockAlert> lowStockAlerts
    ) {}
    
    @Builder
    public record LowStockAlert(
        UUID productId,
        String productName,
        String sku,
        Integer currentStock,
        Integer lowStockThreshold,
        String urgencyLevel // LOW, MEDIUM, HIGH, CRITICAL
    ) {}
    
    @Builder
    public record RecentActivity(
        String activityType, // ORDER_PLACED, PRODUCT_UPDATED, REVIEW_RECEIVED, etc.
        String description,
        Instant timestamp,
        String relatedEntityId,
        String priority // LOW, MEDIUM, HIGH
    ) {}
    
    @Builder
    public record Alert(
        String alertType, // LOW_STOCK, PAYMENT_ISSUE, REVIEW_ALERT, etc.
        String title,
        String message,
        String severity, // INFO, WARNING, ERROR, CRITICAL
        Instant createdAt,
        Boolean isRead,
        String actionUrl
    ) {}
    
    @Builder
    public record PerformanceTrends(
        List<TrendData> salesTrend,
        List<TrendData> orderTrend,
        List<TrendData> customerTrend,
        List<TrendData> profitTrend
    ) {}
    
    @Builder
    public record TrendData(
        LocalDate date,
        BigDecimal value,
        BigDecimal changeFromPrevious
    ) {}
}
