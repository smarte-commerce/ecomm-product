package com.winnguyen1905.product.core.model.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Document Upload Response DTO
 * 
 * Response after vendor document upload for verification
 */
@Builder
public record VendorDocumentUploadResponse(
    UUID documentId,
    UUID vendorId,
    String documentType,
    String category, // IDENTITY, BUSINESS, FINANCIAL, ADDRESS
    
    // Upload Information
    String fileName,
    String originalFileName,
    Long fileSize,
    String mimeType,
    String documentUrl,
    
    // Status Information
    String status, // UPLOADED, UNDER_REVIEW, APPROVED, REJECTED
    String uploadStatus, // SUCCESS, FAILED, PARTIAL
    
    // Processing Information
    ProcessingInformation processingInformation,
    
    // Validation Results
    ValidationResults validationResults,
    
    // Metadata
    DocumentMetadata metadata,
    
    // Timestamps
    Instant uploadedAt,
    Instant lastUpdated,
    
    // Next Steps
    NextSteps nextSteps,
    
    // Error Information (if upload failed)
    ErrorInformation errorInformation
    
) {
    
    @Builder
    public record ProcessingInformation(
        String processingStatus, // PENDING, IN_PROGRESS, COMPLETED, FAILED
        Integer estimatedProcessingTimeMinutes,
        String processingStage, // UPLOAD, VALIDATION, REVIEW, APPROVAL
        Instant processingStarted,
        Instant processingCompleted,
        String assignedReviewer
    ) {}
    
    @Builder
    public record ValidationResults(
        Boolean isValid,
        List<ValidationCheck> validationChecks,
        List<String> warnings,
        List<String> errors,
        Integer validationScore // 0-100
    ) {}
    
    @Builder
    public record ValidationCheck(
        String checkType, // FILE_FORMAT, FILE_SIZE, CONTENT_VALIDATION, OCR_EXTRACTION
        String status, // PASSED, FAILED, WARNING
        String description,
        String details
    ) {}
    
    @Builder
    public record DocumentMetadata(
        // Document details extracted via OCR/AI
        String extractedText,
        ExtractedInformation extractedInformation,
        
        // File properties
        Integer pageCount,
        String resolution,
        Boolean isEncrypted,
        Boolean hasDigitalSignature,
        
        // Security information
        String checksum,
        Boolean virusScanPassed,
        Instant virusScanDate
    ) {}
    
    @Builder
    public record ExtractedInformation(
        // Common fields that might be extracted
        String documentNumber,
        String issuingAuthority,
        LocalDate issueDate,
        LocalDate expiryDate,
        String holderName,
        String address,
        
        // Confidence scores for extracted data
        java.util.Map<String, Double> confidenceScores
    ) {}
    
    @Builder
    public record NextSteps(
        String description,
        List<String> requiredActions,
        LocalDate expectedReviewDate,
        String contactInformation,
        Boolean requiresAdditionalDocuments,
        List<String> additionalDocumentsNeeded
    ) {}
    
    @Builder
    public record ErrorInformation(
        String errorCode,
        String errorMessage,
        String errorDetails,
        List<String> possibleSolutions,
        Boolean isRetryable
    ) {}
}
