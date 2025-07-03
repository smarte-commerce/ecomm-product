package com.winnguyen1905.product.core.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.winnguyen1905.product.secure.RegionPartition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Vendor Registration Request DTO
 * 
 * Comprehensive vendor registration data including business information,
 * contact details, and initial setup preferences
 */
@Builder
public record VendorRegistrationRequest(
    
    // Business Information
    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    String businessName,
    
    @NotBlank(message = "Business type is required")
    String businessType, // INDIVIDUAL, CORPORATION, LLC, PARTNERSHIP, etc.
    
    @Size(max = 500, message = "Business description cannot exceed 500 characters")
    String businessDescription,
    
    @Pattern(regexp = "^[A-Z0-9-]{5,20}$", message = "Tax ID must be 5-20 alphanumeric characters")
    String taxId,
    
    @Pattern(regexp = "^[A-Z0-9-]{5,20}$", message = "Business registration number must be 5-20 alphanumeric characters")
    String businessRegistrationNumber,
    
    LocalDate businessEstablishedDate,
    
    // Contact Information
    @NotBlank(message = "Contact person name is required")
    @Size(min = 2, max = 100, message = "Contact person name must be between 2 and 100 characters")
    String contactPersonName,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    String phoneNumber,
    
    String alternatePhoneNumber,
    
    // Address Information
    @Valid
    @NotNull(message = "Business address is required")
    BusinessAddress businessAddress,
    
    @Valid
    MailingAddress mailingAddress, // Optional, if different from business address
    
    // Banking Information
    @Valid
    @NotNull(message = "Banking information is required")
    BankingInformation bankingInformation,
    
    // Business Preferences
    @NotNull(message = "Region is required")
    RegionPartition region,
    
    List<String> productCategories, // Categories vendor plans to sell
    
    BigDecimal expectedMonthlyRevenue,
    
    String businessWebsite,
    
    List<String> socialMediaLinks,
    
    // Agreement and Verification
    @NotNull(message = "Terms acceptance is required")
    Boolean acceptedTermsAndConditions,
    
    @NotNull(message = "Privacy policy acceptance is required")
    Boolean acceptedPrivacyPolicy,
    
    Boolean subscribeToNewsletter,
    
    // Additional Information
    String referralSource,
    
    String additionalNotes
    
) implements AbstractModel {
    
    @Builder
    public record BusinessAddress(
        @NotBlank(message = "Street address is required")
        String streetAddress,
        
        String addressLine2,
        
        @NotBlank(message = "City is required")
        String city,
        
        @NotBlank(message = "State/Province is required")
        String stateProvince,
        
        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^[A-Z0-9\\s-]{3,10}$", message = "Invalid postal code format")
        String postalCode,
        
        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
        String countryCode
    ) {}
    
    @Builder
    public record MailingAddress(
        @NotBlank(message = "Street address is required")
        String streetAddress,
        
        String addressLine2,
        
        @NotBlank(message = "City is required")
        String city,
        
        @NotBlank(message = "State/Province is required")
        String stateProvince,
        
        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^[A-Z0-9\\s-]{3,10}$", message = "Invalid postal code format")
        String postalCode,
        
        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
        String countryCode
    ) {}
    
    @Builder
    public record BankingInformation(
        @NotBlank(message = "Bank name is required")
        String bankName,
        
        @NotBlank(message = "Account holder name is required")
        String accountHolderName,
        
        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "^[0-9]{8,20}$", message = "Account number must be 8-20 digits")
        String accountNumber,
        
        @NotBlank(message = "Routing number is required")
        @Pattern(regexp = "^[0-9]{9}$", message = "Routing number must be 9 digits")
        String routingNumber,
        
        String swiftCode, // For international transfers
        
        @NotBlank(message = "Account type is required")
        String accountType, // CHECKING, SAVINGS, BUSINESS
        
        String bankAddress
    ) {}
}
