package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.secure.RegionPartition;

import lombok.Builder;

/**
 * Vendor Profile Response DTO
 * 
 * Comprehensive vendor profile information
 */
@Builder
public record VendorProfileResponse(
    UUID vendorId,
    String applicationId,
    
    // Business Information
    String businessName,
    String businessType,
    String businessDescription,
    String taxId,
    String businessRegistrationNumber,
    LocalDate businessEstablishedDate,
    
    // Contact Information
    String contactPersonName,
    String email,
    String phoneNumber,
    String alternatePhoneNumber,
    
    // Address Information
    BusinessAddress businessAddress,
    MailingAddress mailingAddress,
    
    // Banking Information (masked for security)
    BankingInformation bankingInformation,
    
    // Business Preferences
    RegionPartition region,
    List<String> productCategories,
    BigDecimal expectedMonthlyRevenue,
    String businessWebsite,
    List<String> socialMediaLinks,
    
    // Store Information
    StoreInformation storeInformation,
    
    // Status and Verification
    String status, // PENDING, ACTIVE, SUSPENDED, INACTIVE
    String verificationStatus, // UNVERIFIED, PENDING, VERIFIED, REJECTED
    List<String> verificationDocuments,
    
    // Business Hours
    BusinessHours businessHours,
    
    // Statistics
    VendorStatistics statistics,
    
    // Timestamps
    Instant createdAt,
    Instant updatedAt,
    Instant lastLoginAt,
    
    // Additional Information
    String referralSource,
    String additionalNotes
    
) {
    
    @Builder
    public record BusinessAddress(
        String streetAddress,
        String addressLine2,
        String city,
        String stateProvince,
        String postalCode,
        String countryCode,
        String countryName
    ) {}
    
    @Builder
    public record MailingAddress(
        String streetAddress,
        String addressLine2,
        String city,
        String stateProvince,
        String postalCode,
        String countryCode,
        String countryName
    ) {}
    
    @Builder
    public record BankingInformation(
        String bankName,
        String accountHolderName,
        String maskedAccountNumber, // Only last 4 digits shown
        String accountType,
        String bankAddress,
        Boolean isVerified
    ) {}
    
    @Builder
    public record StoreInformation(
        String storeName,
        String storeDescription,
        String storeLogoUrl,
        String storeBannerUrl,
        String storeUrl,
        Boolean isStoreActive,
        BigDecimal storeRating,
        Integer totalReviews
    ) {}
    
    @Builder
    public record BusinessHours(
        DayHours monday,
        DayHours tuesday,
        DayHours wednesday,
        DayHours thursday,
        DayHours friday,
        DayHours saturday,
        DayHours sunday,
        String timezone
    ) {}
    
    @Builder
    public record DayHours(
        Boolean isOpen,
        String openTime,
        String closeTime
    ) {}
    
    @Builder
    public record VendorStatistics(
        Long totalProducts,
        Long activeProducts,
        Long totalOrders,
        BigDecimal totalRevenue,
        BigDecimal averageOrderValue,
        BigDecimal vendorRating,
        Integer totalReviews,
        Instant memberSince
    ) {}
}
