package com.winnguyen1905.product.core.model.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Vendor Settings Update Request DTO
 * 
 * Request for updating vendor-specific settings and preferences
 */
@Builder
public record VendorSettingsUpdateRequest(
    
    // Store Settings
    @Valid
    StoreSettings storeSettings,
    
    // Notification Preferences
    @Valid
    NotificationSettings notificationSettings,
    
    // Payment Settings
    @Valid
    PaymentSettings paymentSettings,
    
    // Shipping Settings
    @Valid
    ShippingSettings shippingSettings,
    
    // Tax Settings
    @Valid
    TaxSettings taxSettings,
    
    // Return Policy Settings
    @Valid
    ReturnPolicySettings returnPolicySettings,
    
    // API and Integration Settings
    @Valid
    IntegrationSettings integrationSettings
    
) implements AbstractModel {
    
    @Builder
    public record StoreSettings(
        @Size(max = 100, message = "Store name cannot exceed 100 characters")
        String storeName,
        
        @Size(max = 1000, message = "Store description cannot exceed 1000 characters")
        String storeDescription,
        
        String storeLogoUrl,
        String storeBannerUrl,
        String storeThemeColor,
        
        Boolean enableStoreReviews,
        Boolean enableProductReviews,
        Boolean enableWishlist,
        Boolean enableCompareProducts,
        
        @Min(value = 1, message = "Minimum order amount must be at least 1")
        @Max(value = 10000, message = "Minimum order amount cannot exceed 10000")
        BigDecimal minimumOrderAmount,
        
        Boolean enableGuestCheckout,
        Boolean requireAccountForPurchase
    ) {}
    
    @Builder
    public record NotificationSettings(
        Boolean enableEmailNotifications,
        Boolean enableSmsNotifications,
        Boolean enablePushNotifications,
        
        // Order Notifications
        Boolean notifyOnNewOrder,
        Boolean notifyOnOrderCancellation,
        Boolean notifyOnOrderReturn,
        Boolean notifyOnPaymentReceived,
        
        // Inventory Notifications
        Boolean notifyOnLowStock,
        Boolean notifyOnOutOfStock,
        Boolean notifyOnInventoryUpdate,
        
        // Review and Rating Notifications
        Boolean notifyOnNewReview,
        Boolean notifyOnNewRating,
        
        // Marketing Notifications
        Boolean enableMarketingEmails,
        Boolean enablePromotionalSms,
        
        // Notification Frequency
        String emailFrequency, // IMMEDIATE, DAILY, WEEKLY
        String smsFrequency     // IMMEDIATE, DAILY, NEVER
    ) {}
    
    @Builder
    public record PaymentSettings(
        List<String> acceptedPaymentMethods, // CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, etc.
        
        Boolean enableInstallmentPayments,
        
        @Min(value = 1, message = "Payment processing delay must be at least 1 day")
        @Max(value = 30, message = "Payment processing delay cannot exceed 30 days")
        Integer paymentProcessingDelayDays,
        
        Boolean enableAutomaticRefunds,
        
        @Min(value = 1, message = "Refund processing time must be at least 1 day")
        @Max(value = 30, message = "Refund processing time cannot exceed 30 days")
        Integer refundProcessingTimeDays,
        
        String preferredCurrency,
        
        Boolean enableMultiCurrency
    ) {}
    
    @Builder
    public record ShippingSettings(
        Boolean enableFreeShipping,
        
        @DecimalMin(value = "0.0", message = "Free shipping threshold must be non-negative")
        @DecimalMax(value = "10000.0", message = "Free shipping threshold cannot exceed 10000")
        BigDecimal freeShippingThreshold,
        
        List<String> supportedShippingMethods, // STANDARD, EXPRESS, OVERNIGHT, PICKUP
        
        @Min(value = 1, message = "Processing time must be at least 1 day")
        @Max(value = 30, message = "Processing time cannot exceed 30 days")
        Integer defaultProcessingTimeDays,
        
        Boolean enableExpressShipping,
        Boolean enableInternationalShipping,
        
        List<String> shippingRegions, // Countries/regions where vendor ships
        
        Boolean enableShippingCalculator,
        Boolean enableRealTimeShippingRates
    ) {}
    
    @Builder
    public record TaxSettings(
        Boolean enableTaxCalculation,
        
        @DecimalMin(value = "0.0", message = "Tax rate must be non-negative")
        @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
        BigDecimal defaultTaxRate,
        
        String taxIdNumber,
        Boolean enableTaxExemptions,
        Boolean includeTaxInPrice,
        
        List<TaxRule> customTaxRules
    ) {}
    
    @Builder
    public record TaxRule(
        String region,
        String productCategory,
        
        @DecimalMin(value = "0.0", message = "Tax rate must be non-negative")
        @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
        BigDecimal taxRate,
        
        Boolean isActive
    ) {}
    
    @Builder
    public record ReturnPolicySettings(
        Boolean enableReturns,
        
        @Min(value = 1, message = "Return window must be at least 1 day")
        @Max(value = 365, message = "Return window cannot exceed 365 days")
        Integer returnWindowDays,
        
        Boolean enableExchanges,
        Boolean enableRefunds,
        
        List<String> returnConditions, // NEW, UNOPENED, ORIGINAL_PACKAGING, etc.
        
        String returnShippingPolicy, // CUSTOMER_PAYS, VENDOR_PAYS, SHARED
        
        Boolean enableReturnLabels,
        Boolean requireReturnApproval,
        
        @Size(max = 1000, message = "Return policy text cannot exceed 1000 characters")
        String returnPolicyText
    ) {}
    
    @Builder
    public record IntegrationSettings(
        Boolean enableApiAccess,
        
        List<String> enabledWebhooks, // ORDER_CREATED, ORDER_UPDATED, PAYMENT_RECEIVED, etc.
        
        String webhookUrl,
        
        @Pattern(regexp = "^[A-Za-z0-9+/=]{20,}$", message = "Invalid webhook secret format")
        String webhookSecret,
        
        Boolean enableThirdPartyIntegrations,
        
        List<ThirdPartyIntegration> thirdPartyIntegrations
    ) {}
    
    @Builder
    public record ThirdPartyIntegration(
        String serviceName, // SHOPIFY, AMAZON, EBAY, etc.
        String apiKey,
        String apiSecret,
        Boolean isActive,
        String syncFrequency // REAL_TIME, HOURLY, DAILY
    ) {}
}
