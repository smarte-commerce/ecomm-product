package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Transaction Response DTO
 * 
 * Individual transaction record for vendor transaction history
 */
@Builder
public record VendorTransactionResponse(
    UUID transactionId,
    String transactionNumber,
    UUID vendorId,
    
    // Transaction Details
    String type, // SALE, REFUND, PAYOUT, FEE, ADJUSTMENT, CHARGEBACK
    String subType, // PRODUCT_SALE, SHIPPING_FEE, PLATFORM_FEE, etc.
    String description,
    
    // Financial Information
    BigDecimal amount,
    String currency,
    String status, // PENDING, COMPLETED, FAILED, CANCELLED
    
    // Related Information
    UUID relatedOrderId,
    String relatedOrderNumber,
    UUID relatedProductId,
    String relatedProductName,
    UUID customerId,
    String customerName,
    
    // Payment Information
    PaymentDetails paymentDetails,
    
    // Fee Breakdown
    FeeBreakdown feeBreakdown,
    
    // Timestamps
    Instant transactionDate,
    Instant processedDate,
    Instant settledDate,
    
    // Additional Information
    String notes,
    String referenceNumber,
    String externalTransactionId,
    
    // Dispute Information
    DisputeInformation disputeInformation
    
) {
    
    @Builder
    public record PaymentDetails(
        String paymentMethod, // CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
        String paymentProvider,
        String paymentProcessorTransactionId,
        BigDecimal processingFee,
        String cardLast4, // For card payments
        String cardBrand // VISA, MASTERCARD, AMEX
    ) {}
    
    @Builder
    public record FeeBreakdown(
        BigDecimal platformFee,
        BigDecimal paymentProcessingFee,
        BigDecimal transactionFee,
        BigDecimal otherFees,
        BigDecimal totalFees,
        BigDecimal netAmount
    ) {}
    
    @Builder
    public record DisputeInformation(
        Boolean hasDispute,
        String disputeStatus, // NONE, PENDING, RESOLVED, LOST
        String disputeReason,
        BigDecimal disputeAmount,
        Instant disputeDate,
        Instant disputeResolvedDate,
        String disputeResolution
    ) {}
}
