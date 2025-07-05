package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Earnings Response DTO
 * 
 * Comprehensive earnings and financial summary for vendors
 */
@Builder
public record VendorEarningsResponse(
    UUID vendorId,
    String vendorName,
    LocalDate startDate,
    LocalDate endDate,
    String currency,
    
    // Earnings Summary
    EarningsSummary earningsSummary,
    
    // Detailed Breakdown
    EarningsBreakdown earningsBreakdown,
    
    // Payout Information
    PayoutInformation payoutInformation,
    
    // Fee Structure
    FeeStructure feeStructure,
    
    // Tax Information
    TaxInformation taxInformation,
    
    // Performance Metrics
    PerformanceMetrics performanceMetrics,
    
    // Historical Data
    List<EarningsDataPoint> historicalEarnings,
    
    Instant generatedAt
    
) {
    
    @Builder
    public record EarningsSummary(
        BigDecimal grossRevenue,
        BigDecimal netRevenue,
        BigDecimal totalFees,
        BigDecimal netEarnings,
        BigDecimal pendingEarnings,
        BigDecimal availableForPayout,
        BigDecimal totalPaidOut,
        
        // Order-related metrics
        Long totalOrders,
        Long paidOrders,
        BigDecimal averageOrderValue,
        BigDecimal conversionRate,
        
        // Refund and return impact
        BigDecimal totalRefunds,
        BigDecimal totalChargebacks,
        BigDecimal refundRate
    ) {}
    
    @Builder
    public record EarningsBreakdown(
        // Revenue sources
        BigDecimal productSales,
        BigDecimal shippingRevenue,
        BigDecimal taxCollected,
        BigDecimal otherRevenue,
        
        // Deductions
        BigDecimal platformFees,
        BigDecimal paymentProcessingFees,
        BigDecimal shippingCosts,
        BigDecimal refundFees,
        BigDecimal chargebackFees,
        BigDecimal otherFees,
        
        // Net calculations
        BigDecimal netProductRevenue,
        BigDecimal netShippingRevenue,
        BigDecimal totalDeductions
    ) {}
    
    @Builder
    public record PayoutInformation(
        BigDecimal currentBalance,
        BigDecimal pendingBalance,
        BigDecimal reservedBalance,
        
        // Next payout
        LocalDate nextPayoutDate,
        BigDecimal nextPayoutAmount,
        String payoutFrequency, // DAILY, WEEKLY, MONTHLY
        
        // Payout method
        String payoutMethod, // BANK_TRANSFER, PAYPAL, CHECK
        String payoutAccountInfo, // Masked account details
        
        // Recent payouts
        List<PayoutRecord> recentPayouts,
        
        // Payout settings
        BigDecimal minimumPayoutAmount,
        Boolean automaticPayouts
    ) {}
    
    @Builder
    public record PayoutRecord(
        UUID payoutId,
        BigDecimal amount,
        LocalDate payoutDate,
        String status, // PENDING, PROCESSING, COMPLETED, FAILED
        String payoutMethod,
        String transactionId,
        String failureReason
    ) {}
    
    @Builder
    public record FeeStructure(
        // Platform fees
        BigDecimal platformFeeRate, // As percentage
        BigDecimal platformFeeAmount,
        
        // Payment processing fees
        BigDecimal paymentProcessingRate,
        BigDecimal paymentProcessingAmount,
        
        // Transaction fees
        BigDecimal transactionFeePerOrder,
        BigDecimal totalTransactionFees,
        
        // Other fees
        BigDecimal listingFees,
        BigDecimal promotionFees,
        BigDecimal subscriptionFees,
        
        // Fee breakdown by category
        List<FeeCategory> feeCategories
    ) {}
    
    @Builder
    public record FeeCategory(
        String categoryName,
        String description,
        BigDecimal amount,
        BigDecimal rate,
        String calculationMethod // PERCENTAGE, FIXED, PER_TRANSACTION
    ) {}
    
    @Builder
    public record TaxInformation(
        BigDecimal taxableIncome,
        BigDecimal taxesCollected,
        BigDecimal taxesRemitted,
        BigDecimal taxLiability,
        
        // Tax breakdown by jurisdiction
        List<TaxJurisdiction> taxByJurisdiction,
        
        // Tax documents
        List<TaxDocument> taxDocuments
    ) {}
    
    @Builder
    public record TaxJurisdiction(
        String jurisdictionName,
        String taxType, // SALES_TAX, VAT, GST
        BigDecimal taxRate,
        BigDecimal taxableAmount,
        BigDecimal taxAmount
    ) {}
    
    @Builder
    public record TaxDocument(
        String documentType, // 1099, TAX_SUMMARY, VAT_RETURN
        String documentUrl,
        LocalDate periodStart,
        LocalDate periodEnd,
        Instant generatedDate
    ) {}
    
    @Builder
    public record PerformanceMetrics(
        BigDecimal profitMargin,
        BigDecimal returnOnInvestment,
        BigDecimal customerLifetimeValue,
        BigDecimal averageOrderProfitability,
        
        // Efficiency metrics
        BigDecimal costPerAcquisition,
        BigDecimal fulfillmentCostRatio,
        BigDecimal marketingROI,
        
        // Growth metrics
        BigDecimal revenueGrowthRate,
        BigDecimal customerGrowthRate,
        BigDecimal orderVolumeGrowthRate
    ) {}
    
    @Builder
    public record EarningsDataPoint(
        LocalDate date,
        String period, // For grouped data
        BigDecimal grossRevenue,
        BigDecimal netEarnings,
        BigDecimal totalFees,
        Long orderCount,
        BigDecimal averageOrderValue
    ) {}
}
