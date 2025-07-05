package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.model.request.OrderStatusUpdateRequest;
import com.winnguyen1905.product.core.model.request.VendorProfileUpdateRequest;
import com.winnguyen1905.product.core.model.request.VendorRegistrationRequest;
import com.winnguyen1905.product.core.model.request.VendorSettingsUpdateRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Vendor Management REST API Controller
 * 
 * Comprehensive vendor operations including registration, profile management,
 * analytics, and vendor-specific business operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
@Validated
@Tag(name = "Vendor Management", description = "APIs for vendor operations and management")
public class VendorController {

  private final VendorProductService vendorProductService;

  // ================== VENDOR REGISTRATION & PROFILE ==================

  @PostMapping("/register")
  @ResponseMessage(message = "Vendor registration submitted successfully")
  @Operation(summary = "Register new vendor", description = "Submit vendor registration application")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Registration submitted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid registration data")
  })
  public ResponseEntity<?> registerVendor(
      @Valid @RequestBody VendorRegistrationRequest vendorRegistrationRequest) {
    log.info("New vendor registration submitted");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(vendorProductService.registerVendor(vendorRegistrationRequest));
  }

  @GetMapping("/profile")
  @ResponseMessage(message = "Get vendor profile success")
  @Operation(summary = "Get vendor profile", description = "Get current vendor's profile information")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getVendorProfile(TAccountRequest accountRequest) {
    log.info("Getting profile for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getVendorProfile(accountRequest.id()));
  }

  @PutMapping("/profile")
  @ResponseMessage(message = "Update vendor profile success")
  @Operation(summary = "Update vendor profile", description = "Update vendor profile information")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> updateVendorProfile(
      @Valid @RequestBody VendorProfileUpdateRequest profileUpdateRequest,
      TAccountRequest accountRequest) {
    log.info("Updating profile for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.updateVendorProfile(accountRequest.id(), profileUpdateRequest));
  }

  // ================== VENDOR ANALYTICS & REPORTING ==================

  @GetMapping("/analytics/dashboard")
  @ResponseMessage(message = "Get vendor dashboard success")
  @Operation(summary = "Get vendor dashboard", description = "Get vendor analytics dashboard data")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getVendorDashboard(
      @Parameter(description = "Date range in days") @RequestParam(defaultValue = "30") Integer days,
      TAccountRequest accountRequest) {
    log.info("Getting dashboard for vendor: {} for {} days", accountRequest.id(), days);
    return ResponseEntity.ok(vendorProductService.getVendorDashboard(accountRequest.id(), days));
  }

  @GetMapping("/analytics/sales")
  @ResponseMessage(message = "Get sales analytics success")
  @Operation(summary = "Get sales analytics", description = "Get detailed sales analytics for vendor")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getSalesAnalytics(
      @Parameter(description = "Start date") @RequestParam LocalDate startDate,
      @Parameter(description = "End date") @RequestParam LocalDate endDate,
      @Parameter(description = "Group by period") @RequestParam(defaultValue = "day") String groupBy,
      TAccountRequest accountRequest) {
    log.info("Getting sales analytics for vendor: {} from {} to {}", accountRequest.id(), startDate, endDate);
    return ResponseEntity.ok(vendorProductService.getSalesAnalytics(accountRequest.id(), startDate, endDate, groupBy));
  }

  @GetMapping("/analytics/products/performance")
  @ResponseMessage(message = "Get product performance success")
  @Operation(summary = "Get product performance", description = "Get top performing products for vendor")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getProductPerformance(
      @Parameter(description = "Number of top products") @RequestParam(defaultValue = "10") Integer limit,
      @Parameter(description = "Sort by metric") @RequestParam(defaultValue = "sales") String sortBy,
      TAccountRequest accountRequest) {
    log.info("Getting product performance for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getProductPerformance(accountRequest.id(), limit, sortBy));
  }

  // ================== VENDOR ORDERS MANAGEMENT ==================

  @GetMapping("/orders")
  @ResponseMessage(message = "Get vendor orders success")
  @Operation(summary = "Get vendor orders", description = "Get orders for vendor products")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<PagedResponse<?>> getVendorOrders(
      @Parameter(description = "Order status filter") @RequestParam(required = false) String status,
      @Parameter(description = "Date range filter") @RequestParam(required = false) String dateRange,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Getting orders for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getVendorOrders(accountRequest.id(), status, dateRange, pageable));
  }

  @GetMapping("/orders/{orderId}")
  @ResponseMessage(message = "Get order details success")
  @Operation(summary = "Get order details", description = "Get detailed information about a specific order")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getOrderDetails(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
      TAccountRequest accountRequest) {
    log.info("Getting order details: {} for vendor: {}", orderId, accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getOrderDetails(orderId, accountRequest.id()));
  }

  @PatchMapping("/orders/{orderId}/status")
  @ResponseMessage(message = "Update order status success")
  @Operation(summary = "Update order status", description = "Update order fulfillment status")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> updateOrderStatus(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
      @Parameter(description = "New status", required = true) @RequestParam OrderStatusUpdateRequest status,
      @Parameter(description = "Status notes") @RequestParam(required = false) String notes,
      TAccountRequest accountRequest) {
    log.info("Updating order {} status to {} for vendor: {}", orderId, status, accountRequest.id());
    return ResponseEntity.ok(vendorProductService.updateOrderStatus(orderId, status, accountRequest.id()));
  }

  // ================== VENDOR FINANCIAL MANAGEMENT ==================

  @GetMapping("/financials/earnings")
  @ResponseMessage(message = "Get earnings summary success")
  @Operation(summary = "Get earnings summary", description = "Get vendor earnings and payout information")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getEarningsSummary(
      @Parameter(description = "Start date") @RequestParam(required = false) LocalDate startDate,
      @Parameter(description = "End date") @RequestParam(required = false) LocalDate endDate,
      TAccountRequest accountRequest) {
    log.info("Getting earnings summary for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getEarningsSummary(accountRequest.id(), startDate, endDate));
  }

  @GetMapping("/financials/transactions")
  @ResponseMessage(message = "Get transaction history success")
  @Operation(summary = "Get transaction history", description = "Get vendor transaction and payout history")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<PagedResponse<?>> getTransactionHistory(
      @Parameter(description = "Transaction type filter") @RequestParam(required = false) String type,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Getting transaction history for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getTransactionHistory(accountRequest.id(), type, pageable));
  }

  // ================== VENDOR SETTINGS & PREFERENCES ==================

  @GetMapping("/settings")
  @ResponseMessage(message = "Get vendor settings success")
  @Operation(summary = "Get vendor settings", description = "Get vendor preferences and settings")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getVendorSettings(TAccountRequest accountRequest) {
    log.info("Getting settings for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getVendorSettings(accountRequest.id()));
  }

  @PutMapping("/settings")
  @ResponseMessage(message = "Update vendor settings success")
  @Operation(summary = "Update vendor settings", description = "Update vendor preferences and settings")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> updateVendorSettings(
      @Valid @RequestBody VendorSettingsUpdateRequest settingsRequest,
      TAccountRequest accountRequest) {
    log.info("Updating settings for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.updateVendorSettings(accountRequest.id(), settingsRequest));
  }

  // ================== VENDOR VERIFICATION & COMPLIANCE ==================

  @PostMapping("/verification/documents")
  @ResponseMessage(message = "Upload verification documents success")
  @Operation(summary = "Upload verification documents", description = "Upload documents for vendor verification")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> uploadVerificationDocuments(
      @Parameter(description = "Document type", required = true) @RequestParam String documentType,
      @Parameter(description = "Document file", required = true) @RequestParam MultipartFile documentFile,
      TAccountRequest accountRequest) {
    log.info("Uploading verification documents for vendor: {}", accountRequest.id());
    return ResponseEntity
        .ok(vendorProductService.uploadVerificationDocuments(accountRequest.id(), documentType, documentFile));
  }

  @GetMapping("/verification/status")
  @ResponseMessage(message = "Get verification status success")
  @Operation(summary = "Get verification status", description = "Get current verification status for vendor")
  @PreAuthorize("hasRole('VENDOR')")
  public ResponseEntity<?> getVerificationStatus(TAccountRequest accountRequest) {
    log.info("Getting verification status for vendor: {}", accountRequest.id());
    return ResponseEntity.ok(vendorProductService.getVerificationStatus(accountRequest.id()));
  }
}
