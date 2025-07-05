package com.winnguyen1905.product.core.model.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Registration Response DTO
 * 
 * Response after vendor registration submission
 */
@Builder
public record VendorRegistrationResponse(
    UUID vendorId,
    String applicationId,
    String businessName,
    String email,
    String status, // PENDING, UNDER_REVIEW, APPROVED, REJECTED
    String message,
    Instant submittedAt,
    Instant estimatedReviewDate,
    NextSteps nextSteps
) {
    
    @Builder
    public record NextSteps(
        String description,
        java.util.List<String> requiredActions,
        java.util.List<String> requiredDocuments,
        String contactEmail,
        String supportPhoneNumber
    ) {}
}
