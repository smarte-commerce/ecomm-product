package com.winnguyen1905.product.core.model.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Vendor Profile Update Request DTO
 * 
 * Request for updating vendor profile information
 * All fields are optional to support partial updates
 */
@Builder
public record VendorProfileUpdateRequest(
    
    // Business Information Updates
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    String businessName,
    
    @Size(max = 500, message = "Business description cannot exceed 500 characters")
    String businessDescription,
    
    // Contact Information Updates
    @Size(min = 2, max = 100, message = "Contact person name must be between 2 and 100 characters")
    String contactPersonName,
    
    @Email(message = "Invalid email format")
    String email,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    String phoneNumber,
    
    String alternatePhoneNumber,
    
    // Address Updates
    @Valid
    BusinessAddressUpdate businessAddress,
    
    @Valid
    MailingAddressUpdate mailingAddress,
    
    // Banking Information Updates
    @Valid
    BankingInformationUpdate bankingInformation,
    
    // Business Preferences Updates
    List<String> productCategories,
    
    BigDecimal expectedMonthlyRevenue,
    
    String businessWebsite,
    
    List<String> socialMediaLinks,
    
    // Profile Settings
    Boolean subscribeToNewsletter,
    
    Boolean enableEmailNotifications,
    
    Boolean enableSmsNotifications,
    
    // Store Settings
    String storeName,
    
    @Size(max = 1000, message = "Store description cannot exceed 1000 characters")
    String storeDescription,
    
    String storeLogoUrl,
    
    String storeBannerUrl,
    
    // Business Hours
    @Valid
    BusinessHours businessHours,
    
    // Additional Information
    String additionalNotes
    
) implements AbstractModel {
    
    @Builder
    public record BusinessAddressUpdate(
        String streetAddress,
        String addressLine2,
        String city,
        String stateProvince,
        @Pattern(regexp = "^[A-Z0-9\\s-]{3,10}$", message = "Invalid postal code format")
        String postalCode,
        @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
        String countryCode
    ) {}
    
    @Builder
    public record MailingAddressUpdate(
        String streetAddress,
        String addressLine2,
        String city,
        String stateProvince,
        @Pattern(regexp = "^[A-Z0-9\\s-]{3,10}$", message = "Invalid postal code format")
        String postalCode,
        @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
        String countryCode
    ) {}
    
    @Builder
    public record BankingInformationUpdate(
        String bankName,
        String accountHolderName,
        @Pattern(regexp = "^[0-9]{8,20}$", message = "Account number must be 8-20 digits")
        String accountNumber,
        @Pattern(regexp = "^[0-9]{9}$", message = "Routing number must be 9 digits")
        String routingNumber,
        String swiftCode,
        String accountType,
        String bankAddress
    ) {}
    
    @Builder
    public record BusinessHours(
        DayHours monday,
        DayHours tuesday,
        DayHours wednesday,
        DayHours thursday,
        DayHours friday,
        DayHours saturday,
        DayHours sunday
    ) {}
    
    @Builder
    public record DayHours(
        Boolean isOpen,
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format (HH:MM)")
        String openTime,
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format (HH:MM)")
        String closeTime
    ) {}
}
