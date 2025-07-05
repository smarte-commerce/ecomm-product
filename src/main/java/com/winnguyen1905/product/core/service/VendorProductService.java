package com.winnguyen1905.product.core.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.winnguyen1905.product.core.model.request.OrderStatusUpdateRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.request.VendorProfileUpdateRequest;
import com.winnguyen1905.product.core.model.request.VendorRegistrationRequest;
import com.winnguyen1905.product.core.model.request.VendorSettingsUpdateRequest;
import com.winnguyen1905.product.core.model.response.VendorDashboardResponse;
import com.winnguyen1905.product.core.model.response.VendorDocumentUploadResponse;
import com.winnguyen1905.product.core.model.response.VendorEarningsResponse;
import com.winnguyen1905.product.core.model.response.VendorOrderResponse;
import com.winnguyen1905.product.core.model.response.VendorProductPerformanceResponse;
import com.winnguyen1905.product.core.model.response.VendorProfileResponse;
import com.winnguyen1905.product.core.model.response.VendorRegistrationResponse;
import com.winnguyen1905.product.core.model.response.VendorSalesAnalyticsResponse;
import com.winnguyen1905.product.core.model.response.VendorSettingsResponse;
import com.winnguyen1905.product.core.model.response.VendorTransactionResponse;
import com.winnguyen1905.product.core.model.response.VendorVerificationResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.secure.TAccountRequest;

/**
 * Vendor Product Service Interface
 * 
 * Service for vendor-specific product operations and Elasticsearch integration
 * 
 * MIGRATION GUIDE:
 * - For product creation: Use EnhancedProductService.createProduct() instead of
 * addProduct()
 * - For product updates: Use EnhancedProductService.updateProduct() instead of
 * updateProduct()
 * - For product deletion: Use EnhancedProductService.deleteProduct() instead of
 * deleteProduct()
 * 
 * This service now primarily handles Elasticsearch indexing via
 * persistProductVariants()
 */
public interface VendorProductService {

  /**
   * Persist product variants to Elasticsearch
   * Used internally by product creation/update processes
   */
  void persistProductVariants(EProduct product);

  /**
   * Update existing product
   * 
   * @deprecated Use EnhancedProductService.updateProduct() instead
   */
  @Deprecated
  void updateProduct(TAccountRequest accountRequest, UpdateProductRequest updateProductRequest);

  /**
   * Delete product
   * 
   * @deprecated Use EnhancedProductService.deleteProduct() instead
   */
  @Deprecated
  void deleteProduct(TAccountRequest accountRequest, UUID productId);

  /**
   * Register a new vendor
   */
  VendorRegistrationResponse registerVendor(VendorRegistrationRequest vendorRegistrationRequest);

  /**
   * Get vendor profile by vendor id
   */
  VendorProfileResponse getVendorProfile(UUID vendorId);

  /**
   * Update vendor profile information
   */
  VendorProfileResponse updateVendorProfile(UUID vendorId, VendorProfileUpdateRequest profileUpdateRequest);

  /**
   * Get vendor dashboard analytics for a period of days
   */
  VendorDashboardResponse getVendorDashboard(UUID vendorId, Integer days);

  /**
   * Get sales analytics for vendor
   */
  VendorSalesAnalyticsResponse getSalesAnalytics(UUID vendorId, LocalDate startDate, LocalDate endDate, String groupBy);

  /**
   * Get product performance analytics for vendor
   */
  VendorProductPerformanceResponse getProductPerformance(UUID vendorId, Integer limit, String sortBy);

  /**
   * Get vendor orders with paging support
   */
  PagedResponse<VendorOrderResponse> getVendorOrders(UUID vendorId, String status,
      String dateRange, Pageable pageable);

  /**
   * Get order details for a vendor order
   */
  VendorOrderResponse getOrderDetails(UUID orderId, UUID vendorId);

  /**
   * Update order status for a vendor order
   */
  VendorOrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest updateRequest, UUID vendorId);

  /**
   * Get earnings summary for vendor
   */
  VendorEarningsResponse getEarningsSummary(UUID vendorId, LocalDate startDate, LocalDate endDate);

  /**
   * Get transaction history for vendor
   */
  PagedResponse<VendorTransactionResponse> getTransactionHistory(UUID vendorId, String type,
      Pageable pageable);

  /**
   * Get vendor settings
   */
  VendorSettingsResponse getVendorSettings(UUID vendorId);

  /**
   * Update vendor settings
   */
  VendorSettingsResponse updateVendorSettings(UUID vendorId, VendorSettingsUpdateRequest settingsRequest);

  /**
   * Upload vendor verification documents
   */
  VendorDocumentUploadResponse uploadVerificationDocuments(UUID vendorId, String documentType, MultipartFile documentFile);

  /**
   * Get verification status for vendor
   */
  VendorVerificationResponse getVerificationStatus(UUID vendorId);
}
