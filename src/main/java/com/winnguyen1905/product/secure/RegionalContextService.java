package com.winnguyen1905.product.secure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service for handling regional context from gateway headers and JWT tokens.
 * Provides utility methods for regional operations and data filtering.
 */
@Service
@Slf4j
public class RegionalContextService {

  // Gateway header constants
  public static final String REGION_HEADER = "X-User-Region";
  public static final String CLIENT_IP_HEADER = "X-Client-IP";
  public static final String REGION_CODE_HEADER = "X-Region-Code";
  public static final String REGION_TIMEZONE_HEADER = "X-Region-Timezone";

  /**
   * Get current user's region from gateway headers or JWT context
   */
  public RegionPartition getCurrentRegion() {
    try {
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();

        // Check gateway regional headers first (most reliable)
        String regionCode = request.getHeader(REGION_CODE_HEADER);
        if (regionCode != null && !regionCode.trim().isEmpty()) {
          return RegionPartition.fromCode(regionCode);
        }

        // Check legacy region header
        String regionName = request.getHeader(REGION_HEADER);
        if (regionName != null && !regionName.trim().isEmpty()) {
          return mapDisplayNameToRegion(regionName);
        }
      }

      log.debug("No regional context found, using default region: US");
      return RegionPartition.US;

    } catch (Exception e) {
      log.warn("Error extracting regional context: {}", e.getMessage());
      return RegionPartition.US;
    }
  }

  /**
   * Get client IP from gateway headers
   */
  public String getClientIp() {
    try {
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        String clientIp = request.getHeader(CLIENT_IP_HEADER);
        if (clientIp != null && !clientIp.trim().isEmpty()) {
          return clientIp;
        }
      }
      return "unknown";
    } catch (Exception e) {
      log.warn("Error extracting client IP: {}", e.getMessage());
      return "unknown";
    }
  }

  /**
   * Get region timezone from gateway headers
   */
  public String getRegionTimezone() {
    try {
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        String timezone = request.getHeader(REGION_TIMEZONE_HEADER);
        if (timezone != null && !timezone.trim().isEmpty()) {
          return timezone;
        }
      }
      return getCurrentRegion().getTimeZone();
    } catch (Exception e) {
      log.warn("Error extracting region timezone: {}", e.getMessage());
      return getCurrentRegion().getTimeZone();
    }
  }

  /**
   * Check if request is from a specific region
   */
  public boolean isFromRegion(RegionPartition region) {
    return getCurrentRegion() == region;
  }

  /**
   * Get all gateway headers for debugging
   */
  public String getGatewayHeaders() {
    try {
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();

        StringBuilder headers = new StringBuilder();
        headers.append("Region Code: ").append(request.getHeader(REGION_CODE_HEADER)).append(", ");
        headers.append("Region Name: ").append(request.getHeader(REGION_HEADER)).append(", ");
        headers.append("Client IP: ").append(request.getHeader(CLIENT_IP_HEADER)).append(", ");
        headers.append("Timezone: ").append(request.getHeader(REGION_TIMEZONE_HEADER));

        return headers.toString();
      }
      return "No request context available";
    } catch (Exception e) {
      return "Error extracting headers: " + e.getMessage();
    }
  }

  /**
   * Map display name to region (for backward compatibility)
   */
  private RegionPartition mapDisplayNameToRegion(String displayName) {
    if (displayName == null)
      return RegionPartition.US;

    return switch (displayName.toLowerCase()) {
      case "united states", "north america" -> RegionPartition.US;
      case "europe", "european union" -> RegionPartition.EU;
      case "asia pacific", "asia", "oceania" -> RegionPartition.ASIA;
      default -> RegionPartition.US;
    };
  }
}
