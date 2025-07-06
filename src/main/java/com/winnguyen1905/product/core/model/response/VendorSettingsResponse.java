package com.winnguyen1905.product.core.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

/**
 * Vendor Settings Response DTO
 * 
 * Comprehensive vendor settings and preferences
 */
@Builder
public record VendorSettingsResponse(
    UUID vendorId,
    String vendorName,
    
    // Store Settings
    StoreSettings storeSettings,
    
    // Notification Preferences
    NotificationSettings notificationSettings,
    
    // Payment Settings
    PaymentSettings paymentSettings,
    
    // Shipping Settings
    ShippingSettings shippingSettings,
    
    // Tax Settings
    TaxSettings taxSettings,
    
    // Return Policy Settings
    ReturnPolicySettings returnPolicySettings,
    
    // API and Integration Settings
    IntegrationSettings integrationSettings,
    
    // Security Settings
    SecuritySettings securitySettings,
    
    Instant lastUpdated,
    String updatedBy
    
) {
    
    @Builder
    public record StoreSettings(
        String storeName,
        String storeDescription,
        String storeLogoUrl,
        String storeBannerUrl,
        String storeThemeColor,
        String storeUrl,
        
        Boolean enableStoreReviews,
        Boolean enableProductReviews,
        Boolean enableWishlist,
        Boolean enableCompareProducts,
        
        BigDecimal minimumOrderAmount,
        Boolean enableGuestCheckout,
        Boolean requireAccountForPurchase,
        
        // Store hours
        StoreHours storeHours,
        
        // Store policies
        String termsOfService,
        String privacyPolicy,
        String shippingPolicy,
        String returnPolicy
    ) {}
    
    @Builder
    public record StoreHours(
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
        String emailFrequency,
        String smsFrequency,
        
        // Contact preferences
        String primaryEmail,
        String primaryPhone,
        List<String> additionalEmails
    ) {}
    
    @Builder
    public record PaymentSettings(
        List<String> acceptedPaymentMethods,
        Boolean enableInstallmentPayments,
        Integer paymentProcessingDelayDays,
        Boolean enableAutomaticRefunds,
        Integer refundProcessingTimeDays,
        String preferredCurrency,
        Boolean enableMultiCurrency,
        List<String> supportedCurrencies,
        
        // Payout settings
        String payoutFrequency,
        BigDecimal minimumPayoutAmount,
        Boolean automaticPayouts,
        
        // Payment provider settings
        List<PaymentProvider> paymentProviders
    ) {}
    
    @Builder
    public record PaymentProvider(
        String providerName,
        Boolean isActive,
        String accountId,
        String status,
        List<String> supportedMethods
    ) {}
    
    @Builder
    public record ShippingSettings(
        Boolean enableFreeShipping,
        BigDecimal freeShippingThreshold,
        List<String> supportedShippingMethods,
        Integer defaultProcessingTimeDays,
        Boolean enableExpressShipping,
        Boolean enableInternationalShipping,
        List<String> shippingRegions,
        Boolean enableShippingCalculator,
        Boolean enableRealTimeShippingRates,
        
        // Shipping providers
        List<ShippingProvider> shippingProviders,
        
        // Default shipping rates
        List<ShippingRate> defaultShippingRates
    ) {}
    
    @Builder
    public record ShippingProvider(
        String providerName,
        Boolean isActive,
        String accountId,
        String apiKey,
        List<String> supportedServices
    ) {}
    
    @Builder
    public record ShippingRate(
        String region,
        String shippingMethod,
        BigDecimal rate,
        BigDecimal freeShippingThreshold,
        Integer estimatedDeliveryDays
    ) {}
    
    @Builder
    public record TaxSettings(
        Boolean enableTaxCalculation,
        BigDecimal defaultTaxRate,
        String taxIdNumber,
        Boolean enableTaxExemptions,
        Boolean includeTaxInPrice,
        List<TaxRule> customTaxRules,
        
        // Tax reporting
        Boolean enableTaxReporting,
        String taxReportingFrequency,
        String taxJurisdiction
    ) {}
    
    @Builder
    public record TaxRule(
        String region,
        String productCategory,
        BigDecimal taxRate,
        Boolean isActive,
        String description
    ) {}
    
    @Builder
    public record ReturnPolicySettings(
        Boolean enableReturns,
        Integer returnWindowDays,
        Boolean enableExchanges,
        Boolean enableRefunds,
        List<String> returnConditions,
        String returnShippingPolicy,
        Boolean enableReturnLabels,
        Boolean requireReturnApproval,
        String returnPolicyText,
        
        // Return processing
        Integer returnProcessingTimeDays,
        Boolean enableReturnTracking,
        List<String> nonReturnableCategories
    ) {}
    
    @Builder
    public record IntegrationSettings(
        Boolean enableApiAccess,
        String apiVersion,
        List<String> enabledWebhooks,
        String webhookUrl,
        Boolean webhookSecretConfigured,
        Boolean enableThirdPartyIntegrations,
        List<ThirdPartyIntegration> thirdPartyIntegrations,
        
        // API usage
        Integer dailyApiLimit,
        Integer currentApiUsage,
        Instant lastApiCall
    ) {}
    
    @Builder
    public record ThirdPartyIntegration(
        String serviceName,
        Boolean isActive,
        String status,
        String syncFrequency,
        Instant lastSync,
        String lastSyncStatus
    ) {}
    
    @Builder
    public record SecuritySettings(
        Boolean enableTwoFactorAuth,
        Boolean requireStrongPasswords,
        Integer sessionTimeoutMinutes,
        Boolean enableLoginNotifications,
        Boolean enableIpWhitelisting,
        List<String> whitelistedIps,
        
        // Data protection
        Boolean enableDataEncryption,
        Boolean enableAuditLogging,
        Integer dataRetentionDays,
        
        // Access control
        List<UserRole> userRoles,
        List<ApiKey> apiKeys
    ) {}
    
    @Builder
    public record UserRole(
        String roleName,
        String description,
        List<String> permissions,
        Boolean isActive
    ) {}
    
    @Builder
    public record ApiKey(
        String keyId,
        String keyName,
        String maskedKey,
        List<String> permissions,
        Boolean isActive,
        Instant createdAt,
        Instant lastUsed
    ) {}
}
