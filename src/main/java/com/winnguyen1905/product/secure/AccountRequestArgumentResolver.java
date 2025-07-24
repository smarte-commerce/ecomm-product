package com.winnguyen1905.product.secure;

import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.security.core.GrantedAuthority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import com.winnguyen1905.product.service.RegionCacheService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountRequestArgumentResolver implements HandlerMethodArgumentResolver {

  private final RegionCacheService regionCacheService;

  public static enum AccountRequestArgument {
    ID("sub"), USERNAME("username"), ROLE("role"), REGION("region");

    String value;

    AccountRequestArgument(String value) {
      this.value = value;
    }
  };

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AccountRequest.class) &&
        parameter.getParameterType().equals(TAccountRequest.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    
    log.debug("Resolving account request argument from headers and authentication context");
    
    // Extract region using the enhanced multi-factor detection
    RegionPartition region = extractRegionWithMultiFactorDetection(webRequest);
    
    // Extract other account information
    String username = null;
    UUID id = null;
    AccountType accountType = AccountType.CUSTOMER; // Default
    
    try {
      // Try to extract from JWT token if available (preferred method)
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication instanceof JwtAuthenticationToken) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtAuth.getToken();
        
        // Extract from Keycloak JWT claims
        username = jwt.getClaimAsString("preferred_username");
        String subjectId = jwt.getClaimAsString("sub");
        if (subjectId != null) {
          id = UUID.fromString(subjectId);
        }
        
        // Extract roles from authorities (already processed by SecurityConfig)
        Collection<? extends GrantedAuthority> authorities = jwtAuth.getAuthorities();
        if (!authorities.isEmpty()) {
          // Convert first authority back to AccountType
          String role = authorities.iterator().next().getAuthority();
          if (role.startsWith("ROLE_")) {
            role = role.substring(5); // Remove "ROLE_" prefix
          }
          accountType = AccountType.fromRole(role);
        }
        
        log.debug("Extracted user info from JWT - User: {}, ID: {}, Type: {}", username, id, accountType);
      }
    } catch (Exception e) {
      log.debug("No valid JWT authentication found, using header-based approach: {}", e.getMessage());
    }
    
    // Fallback to headers if JWT is not available (Gateway scenario)
    if (username == null) {
      username = webRequest.getHeader("X-User-Preferred-Username");
      if (username == null) {
        username = webRequest.getHeader("X-User-Username"); // Backward compatibility
      }
    }
    if (id == null) {
      String userIdHeader = webRequest.getHeader("X-User-ID");
      if (userIdHeader != null) {
        try {
          id = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
          log.warn("Invalid user ID in header: {}", userIdHeader);
          id = UUID.randomUUID(); // Generate temporary ID
        }
      } else {
        id = UUID.randomUUID(); // Generate temporary ID for anonymous users
      }
    }
    if (accountType == AccountType.CUSTOMER) {
      String roleHeader = webRequest.getHeader("X-User-Roles");
      if (roleHeader != null) {
        // Take the first role if multiple roles are provided
        String firstRole = roleHeader.split(",")[0].trim();
        accountType = AccountType.fromRole(firstRole);
      }
    }

    log.info("Resolved account request - User: {}, Region: {}, Type: {}, Detection: {}", 
             username, region.getCode(), accountType, 
             webRequest.getHeader("X-Region-Detection-Method"));

    return TAccountRequest.builder()
        .id(id)
        .region(region)
        .username(username)
        .accountType(accountType)
        .build();
  }

  /**
   * Enhanced region extraction using multiple detection factors with priority order
   */
  private RegionPartition extractRegionWithMultiFactorDetection(NativeWebRequest webRequest) {
    
    // Priority 1: Explicit region preference header (highest priority)
    String preferredRegion = webRequest.getHeader("X-Preferred-Region");
    if (isValidRegionCode(preferredRegion)) {
      log.debug("Using preferred region from header: {}", preferredRegion);
      return RegionPartition.fromCode(preferredRegion);
    }
    
    // Priority 2: Gateway-detected region header (most reliable)
    String gatewayRegion = webRequest.getHeader("X-Region-Code");
    if (isValidRegionCode(gatewayRegion)) {
      String detectionMethod = webRequest.getHeader("X-Region-Detection-Method");
      log.debug("Using gateway-detected region: {} via {}", gatewayRegion, detectionMethod);
      return RegionPartition.fromCode(gatewayRegion);
    }

    // Priority 3: User region from authentication context (JWT token)
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication instanceof JwtAuthenticationToken) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtAuth.getToken();
        String jwtRegion = jwt.getClaimAsString(AccountRequestArgument.REGION.value);
        if (isValidRegionCode(jwtRegion)) {
          log.debug("Using region from JWT token: {}", jwtRegion);
          return RegionPartition.fromCode(jwtRegion);
        }
      }
    } catch (Exception e) {
      log.debug("No JWT token available for region extraction: {}", e.getMessage());
    }
    
    // Priority 4: Session-cached region from Redis (product service has regionCacheService)
    String sessionId = extractSessionId(webRequest);
    if (sessionId != null) {
      try {
        RegionPartition cachedRegion = regionCacheService.getCachedSessionRegion(sessionId);
        if (cachedRegion != null) {
          log.debug("Using cached session region: {}", cachedRegion);
          return cachedRegion;
        }
      } catch (Exception e) {
        log.debug("Error retrieving cached session region: {}", e.getMessage());
      }
    }
    
    // Priority 5: Accept-Language header analysis
    String acceptLanguage = webRequest.getHeader("Accept-Language");
    if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
      RegionPartition languageRegion = extractRegionFromLanguage(acceptLanguage);
      if (languageRegion != null) {
        log.debug("Using region from Accept-Language header: {}", languageRegion);
        return languageRegion;
      }
    }
    
         // Priority 6: Client IP geolocation with Redis caching
     String clientIp = extractClientIp(webRequest);
     if (clientIp != null) {
       try {
         RegionPartition ipRegion = regionCacheService.getCachedRegionForIp(clientIp);
         if (ipRegion != null) {
           log.debug("Using region from client IP {}: {}", clientIp, ipRegion);
           return ipRegion;
         }
       } catch (Exception e) {
         log.debug("Error detecting region from IP: {}", e.getMessage());
       }
     }
    
    // Default fallback
    log.debug("No region detected, defaulting to US");
    return RegionPartition.US;
  }

  /**
   * Extract region from Accept-Language header
   */
  private RegionPartition extractRegionFromLanguage(String acceptLanguage) {
    try {
      // Simple language to region mapping
      if (acceptLanguage.contains("en-US") || acceptLanguage.contains("en-CA") || acceptLanguage.contains("es-MX")) {
        return RegionPartition.US;
      } else if (acceptLanguage.contains("en-GB") || acceptLanguage.contains("de") || 
                 acceptLanguage.contains("fr") || acceptLanguage.contains("it") || 
                 acceptLanguage.contains("es-ES")) {
        return RegionPartition.EU;
      } else if (acceptLanguage.contains("zh") || acceptLanguage.contains("ja") || 
                 acceptLanguage.contains("ko") || acceptLanguage.contains("en-AU") || 
                 acceptLanguage.contains("en-SG")) {
        return RegionPartition.ASIA;
      }
    } catch (Exception e) {
      log.debug("Error parsing Accept-Language header: {}", e.getMessage());
    }
    return null;
  }

  /**
   * Simple IP-based region detection
   */
  private RegionPartition extractRegionFromIp(String ip) {
    try {
      // This is a simplified version - in production you'd use a proper GeoIP service
      if (ip.startsWith("192.168") || ip.startsWith("10.") || ip.startsWith("172.")) {
        return RegionPartition.US; // Local IPs default to US
      }
      
      // Add more sophisticated IP ranges here
      // For now, just return null to proceed to next detection method
      return null;
    } catch (Exception e) {
      log.debug("Error detecting region from IP: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Validate if region code is valid
   */
  private boolean isValidRegionCode(String regionCode) {
    if (regionCode == null || regionCode.trim().isEmpty()) {
      return false;
    }
    try {
      RegionPartition.fromCode(regionCode);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Extract client IP from various headers with priority order
   */
  private String extractClientIp(NativeWebRequest webRequest) {
    // Priority order for IP extraction
    String[] ipHeaders = {
        "X-Client-IP",           // Gateway-provided IP (highest priority)
        "X-Forwarded-For",       // Standard proxy header
        "X-Real-IP",             // Nginx real IP
        "X-Originating-IP",      // Some proxies use this
        "CF-Connecting-IP",      // Cloudflare
        "True-Client-IP",        // Akamai
        "X-Cluster-Client-IP"    // Some load balancers
    };
    
    for (String header : ipHeaders) {
      String ip = webRequest.getHeader(header);
      if (ip != null && !ip.trim().isEmpty() && !ip.equalsIgnoreCase("unknown")) {
        // Handle comma-separated IPs (take the first one)
        if (ip.contains(",")) {
          ip = ip.split(",")[0].trim();
        }
        if (isValidIp(ip)) {
          log.debug("Extracted client IP from {}: {}", header, ip);
          return ip;
        }
      }
    }
    
    return null;
  }

  /**
   * Extract session ID from various sources
   */
  private String extractSessionId(NativeWebRequest webRequest) {
    // Try different session ID sources
    String sessionId = webRequest.getHeader("X-Session-ID");
    if (sessionId != null && !sessionId.trim().isEmpty()) {
      return sessionId;
    }
    
    // Try cookie-based session
    String cookieHeader = webRequest.getHeader("Cookie");
    if (cookieHeader != null) {
      // Simple cookie parsing for JSESSIONID
      String[] cookies = cookieHeader.split(";");
      for (String cookie : cookies) {
        String[] parts = cookie.trim().split("=");
        if (parts.length == 2 && "JSESSIONID".equals(parts[0])) {
          return parts[1];
        }
      }
    }
    
    return null;
  }

  /**
   * Basic IP validation
   */
  private boolean isValidIp(String ip) {
    if (ip == null || ip.trim().isEmpty()) {
      return false;
    }
    
    // Basic IPv4 validation
    String[] parts = ip.split("\\.");
    if (parts.length != 4) {
      return false;
    }
    
    try {
      for (String part : parts) {
        int num = Integer.parseInt(part);
        if (num < 0 || num > 255) {
          return false;
        }
      }
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Cache region information for future requests
   */
  private void cacheRegionForFutureUse(String clientIp, RegionPartition region, String sessionId) {
    try {
      if (clientIp != null && region != null) {
        regionCacheService.cacheIpRegion(clientIp, region);
      }
      // Note: Session region caching is read-only in product service
      log.debug("Cached region {} for IP {}", region != null ? region.getCode() : "null", clientIp);
    } catch (Exception e) {
      log.debug("Failed to cache region information: {}", e.getMessage());
    }
  }

  /**
   * Clear regional context (useful for testing)
   */
  public static void clearRegionalContext() {
    // This method can be used to clear any thread-local regional context if needed
    log.debug("Clearing regional context");
  }
}
