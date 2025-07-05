package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Order Response DTO
 * 
 * Detailed order information for vendor order management
 */
@Builder
public record VendorOrderResponse(
    UUID orderId,
    String orderNumber,
    UUID customerId,
    String customerName,
    String customerEmail,
    
    // Order Status
    String orderStatus, // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, RETURNED
    String fulfillmentStatus, // UNFULFILLED, PARTIAL, FULFILLED
    String paymentStatus, // PENDING, PAID, FAILED, REFUNDED, PARTIALLY_REFUNDED
    
    // Order Details
    List<OrderItem> orderItems,
    
    // Pricing
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal shippingAmount,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    BigDecimal vendorEarnings, // Amount vendor will receive after platform fees
    
    // Shipping Information
    ShippingAddress shippingAddress,
    BillingAddress billingAddress,
    ShippingDetails shippingDetails,
    
    // Payment Information
    PaymentDetails paymentDetails,
    
    // Timestamps
    Instant orderDate,
    Instant confirmedDate,
    Instant shippedDate,
    Instant deliveredDate,
    Instant estimatedDeliveryDate,
    
    // Notes and Communication
    String customerNotes,
    String vendorNotes,
    List<OrderNote> orderNotes,
    
    // Tracking and Fulfillment
    TrackingInformation trackingInformation,
    
    // Return/Refund Information
    ReturnInformation returnInformation
    
) {
    
    @Builder
    public record OrderItem(
        UUID orderItemId,
        UUID productId,
        UUID productVariantId,
        String productName,
        String variantName,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String productImageUrl,
        
        // Fulfillment status for this item
        String fulfillmentStatus,
        Integer quantityFulfilled,
        Integer quantityPending,
        Integer quantityReturned
    ) {}
    
    @Builder
    public record ShippingAddress(
        String recipientName,
        String streetAddress,
        String addressLine2,
        String city,
        String stateProvince,
        String postalCode,
        String countryCode,
        String countryName,
        String phoneNumber
    ) {}
    
    @Builder
    public record BillingAddress(
        String name,
        String streetAddress,
        String addressLine2,
        String city,
        String stateProvince,
        String postalCode,
        String countryCode,
        String countryName
    ) {}
    
    @Builder
    public record ShippingDetails(
        String shippingMethod,
        String shippingCarrier,
        BigDecimal shippingCost,
        Integer estimatedDeliveryDays,
        Boolean requiresSignature,
        Boolean isInsured,
        BigDecimal insuranceValue
    ) {}
    
    @Builder
    public record PaymentDetails(
        String paymentMethod,
        String paymentProvider,
        String transactionId,
        BigDecimal amountPaid,
        String currency,
        Instant paymentDate,
        String paymentStatus,
        
        // For partial payments or installments
        List<PaymentTransaction> paymentTransactions
    ) {}
    
    @Builder
    public record PaymentTransaction(
        String transactionId,
        BigDecimal amount,
        String status,
        Instant processedDate,
        String paymentMethod
    ) {}
    
    @Builder
    public record OrderNote(
        UUID noteId,
        String noteType, // CUSTOMER, VENDOR, SYSTEM, SUPPORT
        String author,
        String content,
        Instant createdAt,
        Boolean isVisibleToCustomer
    ) {}
    
    @Builder
    public record TrackingInformation(
        String trackingNumber,
        String carrier,
        String trackingUrl,
        String currentStatus,
        String currentLocation,
        Instant lastUpdated,
        List<TrackingEvent> trackingEvents
    ) {}
    
    @Builder
    public record TrackingEvent(
        String status,
        String description,
        String location,
        Instant timestamp
    ) {}
    
    @Builder
    public record ReturnInformation(
        Boolean isReturnable,
        Instant returnDeadline,
        String returnReason,
        String returnStatus, // NONE, REQUESTED, APPROVED, IN_TRANSIT, RECEIVED, PROCESSED
        BigDecimal refundAmount,
        String refundStatus,
        List<ReturnItem> returnItems
    ) {}
    
    @Builder
    public record ReturnItem(
        UUID orderItemId,
        Integer quantityReturned,
        String returnReason,
        String condition, // NEW, USED, DAMAGED
        BigDecimal refundAmount
    ) {}
}
