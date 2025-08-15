package com.winnguyen1905.product.secure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller demonstrating regional product functionality and gateway integration.
 * Shows how Product Service leverages enhanced gateway's regional routing.
 */
@RestController
@RequestMapping("/api/v1/regional")
@RequiredArgsConstructor
@Slf4j
public class CRegionalProductController {

    private final RegionalContextService regionalContextService;

    /**
     * Get current regional context from gateway headers
     */
    @GetMapping("/context")
    public Map<String, Object> getRegionalContext() {
        RegionPartition region = regionalContextService.getCurrentRegion();
        String clientIp = regionalContextService.getClientIp();
        String timezone = regionalContextService.getRegionTimezone();
        String gatewayHeaders = regionalContextService.getGatewayHeaders();

        Map<String, Object> context = new HashMap<>();
        context.put("detectedRegion", Map.of(
            "code", region.getCode(),
            "displayName", region.getDisplayName(),
            "timezone", region.getTimeZone()
        ));
        context.put("clientIp", clientIp);
        context.put("regionTimezone", timezone);
        context.put("gatewayHeaders", gatewayHeaders);
        context.put("timestamp", System.currentTimeMillis());

        log.info("Regional context requested - Region: {}, Client IP: {}", region.getCode(), clientIp);
        return context;
    }

    /**
     * Test endpoint to verify regional routing works
     */
    @GetMapping("/test/{region}")
    public Map<String, Object> testRegionalRouting(@PathVariable String region) {
        RegionPartition currentRegion = regionalContextService.getCurrentRegion();
        RegionPartition requestedRegion = RegionPartition.fromCode(region);

        Map<String, Object> response = new HashMap<>();
        response.put("requestedRegion", requestedRegion.getCode());
        response.put("detectedRegion", currentRegion.getCode());
        response.put("routingMatch", currentRegion == requestedRegion);
        response.put("message", String.format("Request routed to %s region service, detected region: %s", 
                                            requestedRegion.getCode(), currentRegion.getCode()));

        log.info("Regional routing test - Requested: {}, Detected: {}, Match: {}", 
                requestedRegion.getCode(), currentRegion.getCode(), currentRegion == requestedRegion);
        
        return response;
    }

    /**
     * Get regional product recommendations based on user's region
     */
    @GetMapping("/products/recommendations")
    public Map<String, Object> getRegionalRecommendations() {
        RegionPartition region = regionalContextService.getCurrentRegion();
        
        Map<String, Object> recommendations = new HashMap<>();
        recommendations.put("region", region.getCode());
        recommendations.put("currency", getRegionalCurrency(region));
        recommendations.put("shippingInfo", getRegionalShipping(region));
        recommendations.put("featuredProducts", getRegionalFeaturedProducts(region));
        recommendations.put("localizedContent", getLocalizedContent(region));

        log.info("Regional recommendations requested for region: {}", region.getCode());
        return recommendations;
    }

    /**
     * Health check endpoint showing regional service status
     */
    @GetMapping("/health")
    public Map<String, Object> regionalHealth() {
        RegionPartition region = regionalContextService.getCurrentRegion();
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("region", region.getCode());
        health.put("service", "Product Service - " + region.getDisplayName());
        health.put("gatewayIntegration", "Active");
        health.put("regionalRouting", "Enabled");
        health.put("timestamp", System.currentTimeMillis());

        return health;
    }

    // Helper methods for regional content

    private String getRegionalCurrency(RegionPartition region) {
        return switch (region) {
            case US -> "USD";
            case EU -> "EUR";
            case ASIA -> "SGD";
        };
    }

    private Map<String, Object> getRegionalShipping(RegionPartition region) {
        return switch (region) {
            case US -> Map.of(
                "provider", "FedEx",
                "standardDelivery", "3-5 business days",
                "expressDelivery", "1-2 business days",
                "freeShippingThreshold", 50
            );
            case EU -> Map.of(
                "provider", "DHL",
                "standardDelivery", "2-4 business days",
                "expressDelivery", "1-2 business days",
                "freeShippingThreshold", 40
            );
            case ASIA -> Map.of(
                "provider", "Singapore Post",
                "standardDelivery", "1-3 business days",
                "expressDelivery", "Same day",
                "freeShippingThreshold", 30
            );
        };
    }

    private Map<String, Object> getRegionalFeaturedProducts(RegionPartition region) {
        return switch (region) {
            case US -> Map.of(
                "featured", "Nike Air Max 270",
                "trending", "Athletic Wear",
                "popular", "Running Shoes"
            );
            case EU -> Map.of(
                "featured", "Adidas Ultraboost 21",
                "trending", "Premium Footwear",
                "popular", "European Styles"
            );
            case ASIA -> Map.of(
                "featured", "Nike Dri-FIT T-Shirt",
                "trending", "Activewear",
                "popular", "Lightweight Apparel"
            );
        };
    }

    private Map<String, String> getLocalizedContent(RegionPartition region) {
        return switch (region) {
            case US -> Map.of(
                "language", "en-US",
                "welcome", "Welcome to our US store!",
                "promotion", "Free shipping on orders over $50"
            );
            case EU -> Map.of(
                "language", "en-EU",
                "welcome", "Welcome to our European store!",
                "promotion", "Free shipping on orders over €40"
            );
            case ASIA -> Map.of(
                "language", "en-SG",
                "welcome", "Welcome to our Asia Pacific store!",
                "promotion", "Free shipping on orders over S$30"
            );
        };
    }
} 
