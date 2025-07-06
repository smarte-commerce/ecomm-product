package com.winnguyen1905.product.core.model.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Verification Response DTO
 * 
 * Comprehensive vendor verification status and document information
 */
@Builder
public record VendorVerificationResponse(
    UUID vendorId,
    String vendorName,
    
    // Overall Verification Status
    String overallStatus, // UNVERIFIED, PENDING, VERIFIED, REJECTED, SUSPENDED
    String verificationLevel, // BASIC, STANDARD, PREMIUM
    Integer verificationScore, // 0-100
    
    // Verification Categories
    IdentityVerification identityVerification,
    BusinessVerification businessVerification,
    FinancialVerification financialVerification,
    AddressVerification addressVerification,
    
    // Document Status
    List<DocumentStatus> documentStatuses,
    
    // Verification History
    List<VerificationEvent> verificationHistory,
    
    // Requirements and Next Steps
    VerificationRequirements requirements,
    
    // Compliance Information
    ComplianceInformation compliance,
    
    Instant lastUpdated,
    String updatedBy
    
) {
    
    @Builder
    public record IdentityVerification(
        String status, // PENDING, VERIFIED, REJECTED
        String verificationMethod, // DOCUMENT, BIOMETRIC, THIRD_PARTY
        Instant verifiedDate,
        String verifiedBy,
        
        // Identity documents
        List<IdentityDocument> identityDocuments,
        
        // Verification details
        String fullName,
        LocalDate dateOfBirth,
        String nationality,
        String idNumber,
        
        // Verification notes
        String notes,
        String rejectionReason
    ) {}
    
    @Builder
    public record IdentityDocument(
        String documentType, // PASSPORT, DRIVERS_LICENSE, NATIONAL_ID
        String documentNumber,
        String issuingCountry,
        LocalDate expiryDate,
        String status,
        String documentUrl,
        Instant uploadedDate
    ) {}
    
    @Builder
    public record BusinessVerification(
        String status,
        Instant verifiedDate,
        String verifiedBy,
        
        // Business documents
        List<BusinessDocument> businessDocuments,
        
        // Business details
        String businessName,
        String businessType,
        String registrationNumber,
        String taxId,
        LocalDate incorporationDate,
        String jurisdiction,
        
        // Business address verification
        Boolean addressVerified,
        String addressVerificationMethod,
        
        String notes,
        String rejectionReason
    ) {}
    
    @Builder
    public record BusinessDocument(
        String documentType, // INCORPORATION_CERTIFICATE, TAX_REGISTRATION, BUSINESS_LICENSE
        String documentNumber,
        String issuingAuthority,
        LocalDate issueDate,
        LocalDate expiryDate,
        String status,
        String documentUrl,
        Instant uploadedDate
    ) {}
    
    @Builder
    public record FinancialVerification(
        String status,
        Instant verifiedDate,
        String verifiedBy,
        
        // Bank account verification
        BankAccountVerification bankAccountVerification,
        
        // Financial documents
        List<FinancialDocument> financialDocuments,
        
        // Credit check
        CreditCheck creditCheck,
        
        String notes,
        String rejectionReason
    ) {}
    
    @Builder
    public record BankAccountVerification(
        String status,
        String verificationMethod, // MICRO_DEPOSITS, INSTANT_VERIFICATION, MANUAL
        String bankName,
        String accountType,
        String maskedAccountNumber,
        Boolean isVerified,
        Instant verifiedDate
    ) {}
    
    @Builder
    public record FinancialDocument(
        String documentType, // BANK_STATEMENT, TAX_RETURN, FINANCIAL_STATEMENT
        String period,
        String status,
        String documentUrl,
        Instant uploadedDate
    ) {}
    
    @Builder
    public record CreditCheck(
        String status,
        Integer creditScore,
        String creditRating,
        String creditAgency,
        Instant checkDate,
        Boolean passed
    ) {}
    
    @Builder
    public record AddressVerification(
        String status,
        Instant verifiedDate,
        String verificationMethod, // DOCUMENT, UTILITY_BILL, THIRD_PARTY
        
        // Address details
        String streetAddress,
        String city,
        String stateProvince,
        String postalCode,
        String countryCode,
        
        // Verification documents
        List<AddressDocument> addressDocuments,
        
        String notes,
        String rejectionReason
    ) {}
    
    @Builder
    public record AddressDocument(
        String documentType, // UTILITY_BILL, BANK_STATEMENT, LEASE_AGREEMENT
        LocalDate documentDate,
        String status,
        String documentUrl,
        Instant uploadedDate
    ) {}
    
    @Builder
    public record DocumentStatus(
        UUID documentId,
        String documentType,
        String category, // IDENTITY, BUSINESS, FINANCIAL, ADDRESS
        String status, // PENDING, UNDER_REVIEW, APPROVED, REJECTED, EXPIRED
        String fileName,
        String documentUrl,
        Instant uploadedDate,
        Instant reviewedDate,
        String reviewedBy,
        String rejectionReason,
        LocalDate expiryDate,
        Boolean isExpired
    ) {}
    
    @Builder
    public record VerificationEvent(
        UUID eventId,
        String eventType, // DOCUMENT_UPLOADED, DOCUMENT_APPROVED, DOCUMENT_REJECTED, STATUS_CHANGED
        String description,
        String category,
        String oldStatus,
        String newStatus,
        String performedBy,
        Instant timestamp,
        String notes
    ) {}
    
    @Builder
    public record VerificationRequirements(
        List<RequiredDocument> requiredDocuments,
        List<RequiredAction> requiredActions,
        String nextStepDescription,
        LocalDate deadline,
        Boolean isUrgent
    ) {}
    
    @Builder
    public record RequiredDocument(
        String documentType,
        String category,
        String description,
        Boolean isRequired,
        Boolean isUploaded,
        LocalDate deadline,
        List<String> acceptedFormats
    ) {}
    
    @Builder
    public record RequiredAction(
        String actionType, // UPLOAD_DOCUMENT, VERIFY_PHONE, VERIFY_EMAIL, COMPLETE_PROFILE
        String description,
        String priority, // LOW, MEDIUM, HIGH, CRITICAL
        LocalDate deadline,
        Boolean isCompleted
    ) {}
    
    @Builder
    public record ComplianceInformation(
        List<ComplianceCheck> complianceChecks,
        String riskLevel, // LOW, MEDIUM, HIGH
        String riskScore,
        List<String> complianceFlags,
        Boolean kycCompliant,
        Boolean amlCompliant,
        Instant lastComplianceCheck
    ) {}
    
    @Builder
    public record ComplianceCheck(
        String checkType, // KYC, AML, SANCTIONS, PEP
        String status,
        String result,
        Instant checkDate,
        String provider,
        String referenceId
    ) {}
}
