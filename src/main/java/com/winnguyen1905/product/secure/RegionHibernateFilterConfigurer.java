package com.winnguyen1905.product.secure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.winnguyen1905.product.config.RegionalDataSourceConfiguration;
import com.winnguyen1905.product.secure.AccountRequestArgumentResolver.AccountRequestArgument;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Hibernate filter configurer that automatically enables region-based filters
 * for all queries based on the user's detected region.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegionHibernateFilterConfigurer extends OncePerRequestFilter {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      // Get region from thread-local context (set by AccountRequestArgumentResolver)
      RegionPartition region = RegionalDataSourceConfiguration.RegionalContext.getCurrentRegion();
      
      if (region != null) {
        enableRegionFilter(region);
        log.debug("Enabled Hibernate region filter for region: {}", region.getCode());
      } else {
        // Fallback: try to get region from JWT token
        String regionFromJwt = getRegionFromJwtToken();
        if (regionFromJwt != null) {
          RegionPartition jwtRegion = RegionPartition.fromCode(regionFromJwt);
          enableRegionFilter(jwtRegion);
          log.debug("Enabled Hibernate region filter from JWT for region: {}", jwtRegion.getCode());
        } else {
          // Fallback: try to get region from headers
          String headerRegion = request.getHeader("X-Region-Code");
          if (headerRegion != null && !headerRegion.trim().isEmpty()) {
            RegionPartition headerRegionPartition = RegionPartition.fromCode(headerRegion);
            enableRegionFilter(headerRegionPartition);
            log.debug("Enabled Hibernate region filter from header for region: {}", headerRegionPartition.getCode());
          } else {
            // No region available - queries will access all regions
            disableRegionFilter();
            log.debug("No region detected, disabled region filter - queries will access all regions");
          }
        }
      }
    } catch (Exception e) {
      log.error("Error configuring region filter: {}", e.getMessage());
      // Continue without filter in case of error
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      // Clean up after request processing
      try {
        disableRegionFilter();
        RegionalDataSourceConfiguration.RegionalContext.clear();
      } catch (Exception e) {
        log.warn("Error cleaning up region filter: {}", e.getMessage());
      }
    }
  }

  /**
   * Enable the region filter for the current Hibernate session
   */
  private void enableRegionFilter(RegionPartition region) {
    try {
      Session session = entityManager.unwrap(Session.class);
      
      // Enable region filter
      Filter regionFilter = session.getEnabledFilter("regionFilter");
      if (regionFilter == null) {
        session.enableFilter("regionFilter").setParameter("region", region.getCode());
      } else {
        regionFilter.setParameter("region", region.getCode());
      }
      
      // Also enable vendor filter if vendor ID is available from request
      String vendorId = getCurrentVendorId();
      if (vendorId != null) {
        Filter vendorFilter = session.getEnabledFilter("vendorFilter");
        if (vendorFilter == null) {
          session.enableFilter("vendorFilter").setParameter("vendorId", vendorId);
        } else {
          vendorFilter.setParameter("vendorId", vendorId);
        }
        log.debug("Enabled vendor filter for vendor: {}", vendorId);
      }
      
    } catch (Exception e) {
      log.error("Error enabling region filter: {}", e.getMessage());
    }
  }

  /**
   * Disable the region filter for the current Hibernate session
   */
  private void disableRegionFilter() {
    try {
      Session session = entityManager.unwrap(Session.class);
      
      Filter regionFilter = session.getEnabledFilter("regionFilter");
      if (regionFilter != null) {
        session.disableFilter("regionFilter");
      }
      
      Filter vendorFilter = session.getEnabledFilter("vendorFilter");
      if (vendorFilter != null) {
        session.disableFilter("vendorFilter");
      }
      
    } catch (Exception e) {
      log.warn("Error disabling filters: {}", e.getMessage());
    }
  }

  /**
   * Get region from JWT token as fallback
   */
  private String getRegionFromJwtToken() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
        return jwt.getClaimAsString(AccountRequestArgument.REGION.value);
      }
    } catch (Exception e) {
      log.debug("Could not extract region from JWT: {}", e.getMessage());
    }
    return null;
  }

  /**
   * Get vendor ID from JWT token for vendor filtering
   */
  private String getCurrentVendorId() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
        return jwt.getClaimAsString("vendor_id");
      }
    } catch (Exception e) {
      log.debug("Could not extract vendor ID from JWT: {}", e.getMessage());
    }
    return null;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    
    // Skip filter for certain paths that don't need region filtering
    return path.startsWith("/actuator/") ||
           path.startsWith("/health") ||
           path.startsWith("/metrics") ||
           path.startsWith("/swagger-") ||
           path.startsWith("/v3/api-docs") ||
           path.equals("/favicon.ico");
  }
} 
