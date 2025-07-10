package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.VendorProfileUpdateRequest;
import com.winnguyen1905.product.core.model.request.VendorRegistrationRequest;
import com.winnguyen1905.product.core.model.request.VendorSettingsUpdateRequest;
import com.winnguyen1905.product.core.model.response.VendorDashboardResponse;
import com.winnguyen1905.product.core.model.response.VendorDocumentUploadResponse;
import com.winnguyen1905.product.core.model.response.VendorProductPerformanceResponse;
import com.winnguyen1905.product.core.model.response.VendorProfileResponse;
import com.winnguyen1905.product.core.model.response.VendorRegistrationResponse;
import com.winnguyen1905.product.core.model.response.VendorSettingsResponse;
import com.winnguyen1905.product.core.model.response.VendorVerificationResponse;
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

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Vendor Product Management REST API Controller
 * 
 * Vendor-specific operations for product management, registration, and analytics
 * Focused on product-related vendor operations only
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
@Validated
@Tag(name = "Vendor Product Management", description = "APIs for vendor product operations and management")
public class VendorController extends BaseController {

  private final VendorProductService vendorProductService;

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
  // @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<VendorVerificationResponse> getVerificationStatus(TAccountRequest accountRequest) {
    logRequest("Getting verification status", accountRequest);
    var response = vendorProductService.getVerificationStatus(accountRequest.id());
    return ok(response);
  }
}
