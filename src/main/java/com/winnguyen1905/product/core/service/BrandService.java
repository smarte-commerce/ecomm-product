package com.winnguyen1905.product.core.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.product.core.model.response.BrandResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.secure.TAccountRequest;

/**
 * Service for brand management operations
 */
public interface BrandService {
  /**
   * Create a new brand
   */
  BrandResponse createBrand(Object brandRequest, TAccountRequest accountRequest);
  
  /**
   * Update an existing brand
   */
  BrandResponse updateBrand(UUID brandId, Object brandRequest, TAccountRequest accountRequest);
  
  /**
   * Get a brand by its ID
   */
  BrandResponse getBrandById(UUID brandId);
  
  /**
   * Delete a brand
   */
  void deleteBrand(UUID brandId, TAccountRequest accountRequest);
  
  /**
   * Get all brands with pagination
   */
  PagedResponse<BrandResponse> getAllBrands(Pageable pageable);
  
  /**
   * Get brands for a specific vendor
   */
  PagedResponse<BrandResponse> getVendorBrands(UUID vendorId, Pageable pageable, TAccountRequest accountRequest);
  
  /**
   * Search brands by name or other criteria
   */
  PagedResponse<BrandResponse> searchBrands(String query, Pageable pageable);
  
  /**
   * Legacy method - add a brand
   * @deprecated Use {@link #createBrand(Object, TAccountRequest)} instead
   */
  @Deprecated
  BrandResponse addBrand(UUID userId, BrandResponse brand);
}
