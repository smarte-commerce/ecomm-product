package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.VendorProfileUpdateRequest;
import com.winnguyen1905.product.core.model.request.VendorRegistrationRequest;
import com.winnguyen1905.product.core.model.request.VendorSettingsUpdateRequest;
import com.winnguyen1905.product.core.model.response.ProductResponse;
import com.winnguyen1905.product.core.model.response.VendorDashboardResponse;
import com.winnguyen1905.product.core.model.response.VendorDocumentUploadResponse;
import com.winnguyen1905.product.core.model.response.VendorProductPerformanceResponse;
import com.winnguyen1905.product.core.model.response.VendorProfileResponse;
import com.winnguyen1905.product.core.model.response.VendorRegistrationResponse;
import com.winnguyen1905.product.core.model.response.VendorSettingsResponse;
import com.winnguyen1905.product.core.model.response.VendorVerificationResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.service.EnhancedProductService;
import com.winnguyen1905.product.core.service.VendorProductService;
import com.winnguyen1905.product.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Vendor Management REST API Controller
 * 
 * All vendor operations including registration, profile management,
 * product operations, analytics, and settings
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vendors")
@Tag(name = "Vendor Management", description = "All vendor operations and management")
public class VendorController extends BaseController {

  private final VendorProductService vendorProductService;
  private final EnhancedProductService enhancedProductService;

  // ================== VENDOR REGISTRATION & PROFILE ==================

  @PostMapping("/register")
  @ResponseMessage(message = "Vendor registration submitted successfully")
  @Operation(summary = "Register new vendor", description = "Submit vendor registration application")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Registration submitted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid registration data")
  })
  public ResponseEntity<VendorRegistrationResponse> registerVendor(
      @Valid @RequestBody VendorRegistrationRequest vendorRegistrationRequest) {
    logPublicRequest("New vendor registration request received");
    var response = vendorProductService.registerVendor(vendorRegistrationRequest);
    return created(response);
  }

  @GetMapping("/profile")
  @ResponseMessage(message = "Get vendor profile success")
  @Operation(summary = "Get vendor profile", description = "Get current vendor's profile information")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Vendor profile not found")
  })
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorProfileResponse> getVendorProfile(TAccountRequest accountRequest) {
    logRequest("Getting vendor profile", accountRequest);
    var response = vendorProductService.getVendorProfile(accountRequest.id());
    return ok(response);
  }

  @PutMapping("/profile")
  @ResponseMessage(message = "Update vendor profile success")
  @Operation(summary = "Update vendor profile", description = "Update vendor profile information")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid profile data"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Vendor profile not found")
  })
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorProfileResponse> updateVendorProfile(
      @Valid @RequestBody VendorProfileUpdateRequest profileUpdateRequest,
      TAccountRequest accountRequest) {
    logRequest("Updating vendor profile", accountRequest);
    var response = vendorProductService.updateVendorProfile(accountRequest.id(), profileUpdateRequest);
    return ok(response);
  }

  // ================== VENDOR PRODUCT ANALYTICS ==================

  @GetMapping("/analytics/dashboard")
  @ResponseMessage(message = "Get vendor dashboard success")
  @Operation(summary = "Get vendor dashboard", description = "Get vendor product analytics dashboard data")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorDashboardResponse> getVendorDashboard(
      @Parameter(description = "Date range in days") @RequestParam(defaultValue = "30") Integer days,
      TAccountRequest accountRequest) {
    logRequest("Getting vendor dashboard with " + days + " days", accountRequest);
    var response = vendorProductService.getVendorDashboard(accountRequest.id(), days);
    return ok(response);
  }

  @GetMapping("/analytics/products/performance")
  @ResponseMessage(message = "Get product performance success")
  @Operation(summary = "Get product performance", description = "Get top performing products for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product performance data retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorProductPerformanceResponse> getProductPerformance(
      @Parameter(description = "Number of top products") @RequestParam(defaultValue = "10") Integer limit,
      @Parameter(description = "Sort by metric") @RequestParam(defaultValue = "sales") String sortBy,
      TAccountRequest accountRequest) {
    logRequest("Getting product performance with limit " + limit + " sorted by " + sortBy, accountRequest);
    var response = vendorProductService.getProductPerformance(accountRequest.id(), limit, sortBy);
    return ok(response);
  }

  // ================== VENDOR SETTINGS ==================

  @GetMapping("/settings")
  @ResponseMessage(message = "Get vendor settings success")
  @Operation(summary = "Get vendor settings", description = "Get vendor preferences and settings")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Settings retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorSettingsResponse> getVendorSettings(TAccountRequest accountRequest) {
    logRequest("Getting vendor settings", accountRequest);
    var response = vendorProductService.getVendorSettings(accountRequest.id());
    return ok(response);
  }

  @PutMapping("/settings")
  @ResponseMessage(message = "Update vendor settings success")
  @Operation(summary = "Update vendor settings", description = "Update vendor preferences and settings")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Settings updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid settings data"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorSettingsResponse> updateVendorSettings(
      @Valid @RequestBody VendorSettingsUpdateRequest settingsRequest,
      TAccountRequest accountRequest) {
    logRequest("Updating vendor settings", accountRequest);
    var response = vendorProductService.updateVendorSettings(accountRequest.id(), settingsRequest);
    return ok(response);
  }

  // ================== VENDOR VERIFICATION ==================

  @PostMapping("/verification/documents")
  @ResponseMessage(message = "Upload verification documents success")
  @Operation(summary = "Upload verification documents", description = "Upload documents for vendor verification")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Documents uploaded successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid document or file format"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "413", description = "File too large")
  })
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorDocumentUploadResponse> uploadVerificationDocuments(
      @Parameter(description = "Document type", required = true) @RequestParam String documentType,
      @Parameter(description = "Document file", required = true) @RequestParam MultipartFile documentFile,
      TAccountRequest accountRequest) {
    logRequest("Uploading verification documents of type " + documentType, accountRequest);
    var response = vendorProductService.uploadVerificationDocuments(
        accountRequest.id(), documentType, documentFile);
    return ok(response);
  }

  @GetMapping("/verification/status")
  @ResponseMessage(message = "Get verification status success")
  @Operation(summary = "Get verification status", description = "Get current verification status for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Verification status retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Not authorized to access this resource"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  public ResponseEntity<VendorVerificationResponse> getVerificationStatus(TAccountRequest accountRequest) {
    logRequest("Getting verification status", accountRequest);
    var response = vendorProductService.getVerificationStatus(accountRequest.id());
    return ok(response);
  }

  // ================== VENDOR PRODUCT OPERATIONS ==================

  @GetMapping("/{vendorId}/products")
  @Operation(summary = "Get vendor products", description = "Get products list for specific vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only access own products"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getVendorProducts(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      @Parameter(description = "Product Status Filter") @RequestParam(required = false) ProductStatus status,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    logRequest("Getting products for vendor " + vendorId + " with status " + status, accountRequest);
    
    // Validate vendor access for non-admin users
    if (!isAdmin(accountRequest)) {
      validateVendorAccess(vendorId, accountRequest);
    }
    
    PagedResponse<ProductResponse> response = enhancedProductService.getVendorProducts(
        vendorId, status, pageable, accountRequest);
    return ok(response);
  }

  @GetMapping("/shops/{shopId}/products")
  @Operation(summary = "Get shop products", description = "Get public products for shop display")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Shop products retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Shop not found")
  })
  public ResponseEntity<PagedResponse<ProductResponse>> getShopProducts(
      @Parameter(description = "Shop ID", required = true) @PathVariable UUID shopId,
      @PageableDefault(size = 20) Pageable pageable) {
    logPublicRequest("Getting public products for shop", shopId);
    PagedResponse<ProductResponse> response = enhancedProductService.getShopProducts(shopId, pageable);
    return ok(response);
  }

  @GetMapping("/{vendorId}/products/low-stock")
  @Operation(summary = "Get low stock products", description = "Get products with low stock levels for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Vendor not found")
  })
  public ResponseEntity<List<ProductResponse>> checkLowStockProducts(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      TAccountRequest accountRequest) {
    logRequest("Getting low stock products for vendor", vendorId, accountRequest);
    
    // Validate vendor access for non-admin users
    if (!isAdmin(accountRequest)) {
      validateVendorAccess(vendorId, accountRequest);
    }
    
    List<ProductResponse> response = enhancedProductService.checkLowStockProducts(vendorId);
    return ok(response);
  }

  @PatchMapping("/products/{productId}/seo")
  @ResponseMessage(message = "Update SEO success")
  @Operation(summary = "Update SEO metadata", description = "Update product SEO metadata for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "SEO metadata updated successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only update own products"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> updateProductSEO(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      @Parameter(description = "Meta title") @RequestParam(required = false) String metaTitle,
      @Parameter(description = "Meta description") @RequestParam(required = false) String metaDescription,
      @Parameter(description = "Meta keywords") @RequestParam(required = false) String metaKeywords,
      TAccountRequest accountRequest) {
    logRequest("Updating SEO metadata for product", productId, accountRequest);
    ProductResponse response = enhancedProductService.updateProductSEO(
        productId, metaTitle, metaDescription, metaKeywords, accountRequest);
    return ok(response);
  }

  @PostMapping("/products/{productId}/sync-inventory")
  @ResponseMessage(message = "Sync inventory success")
  @Operation(summary = "Sync inventory", description = "Sync product inventory with Elasticsearch for vendor")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Inventory synchronized successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - can only sync own products"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<Void> syncProductInventory(
      @Parameter(description = "Product ID", required = true) @PathVariable UUID productId,
      TAccountRequest accountRequest) {
    logRequest("Syncing inventory for product", productId, accountRequest);
    enhancedProductService.syncProductInventory(productId);
    return noContent();
  }
}
