package com.winnguyen1905.product.core.model.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Order Status Update Request DTO
 * 
 * Request for updating order status by vendors
 */
@Builder
public record OrderStatusUpdateRequest(
    
    @NotBlank(message = "Order status is required")
    String status, // CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    String notes,
    
    // Tracking information (for shipped status)
    TrackingInformation trackingInformation,
    
    // Fulfillment details
    List<ItemFulfillment> itemFulfillments,
    
    // Estimated delivery (for processing/shipped status)
    Instant estimatedDeliveryDate,
    
    // Cancellation details (for cancelled status)
    CancellationDetails cancellationDetails,
    
    // Internal vendor notes (not visible to customer)
    @Size(max = 500, message = "Internal notes cannot exceed 500 characters")
    String internalNotes,
    
    // Notification preferences
    Boolean notifyCustomer,
    Boolean sendTrackingEmail
    
) implements AbstractModel {
    
    @Builder
    public record TrackingInformation(
        @NotBlank(message = "Tracking number is required")
        String trackingNumber,
        
        @NotBlank(message = "Carrier is required")
        String carrier,
        
        String trackingUrl,
        String shippingMethod,
        Instant shippedDate
    ) {}
    
    @Builder
    public record ItemFulfillment(
        @NotNull(message = "Order item ID is required")
        java.util.UUID orderItemId,
        
        @NotNull(message = "Quantity fulfilled is required")
        Integer quantityFulfilled,
        
        String fulfillmentNotes
    ) {}
    
    @Builder
    public record CancellationDetails(
        @NotBlank(message = "Cancellation reason is required")
        String reason, // OUT_OF_STOCK, CUSTOMER_REQUEST, VENDOR_ISSUE, PAYMENT_FAILED
        
        @Size(max = 500, message = "Cancellation notes cannot exceed 500 characters")
        String notes,
        
        Boolean refundIssued,
        String refundMethod,
        java.math.BigDecimal refundAmount
    ) {}
}
