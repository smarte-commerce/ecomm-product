package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Customer Management REST API Controller
 * 
 * Customer profile management, wishlist, addresses, and preferences
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer Management", description = "APIs for customer operations")
public class CustomerController {

  // private final CustomerService customerService;

  // ================== CUSTOMER PROFILE ==================

  @GetMapping("/profile")
  @ResponseMessage(message = "Get customer profile success")
  @Operation(summary = "Get customer profile", description = "Get current customer's profile information")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> getCustomerProfile(TAccountRequest accountRequest) {
    log.info("Getting profile for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.getCustomerProfile(accountRequest.id()));
    return ResponseEntity.ok("Customer profile");
  }

  @PutMapping("/profile")
  @ResponseMessage(message = "Update customer profile success")
  @Operation(summary = "Update customer profile", description = "Update customer profile information")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> updateCustomerProfile(
      @Valid @RequestBody Object profileUpdateRequest,
      TAccountRequest accountRequest) {
    log.info("Updating profile for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.updateCustomerProfile(accountRequest.id(), profileUpdateRequest));
    return ResponseEntity.ok("Profile updated");
  }

  // ================== CUSTOMER ADDRESSES ==================

  @GetMapping("/addresses")
  @ResponseMessage(message = "Get customer addresses success")
  @Operation(summary = "Get customer addresses", description = "Get all saved addresses for customer")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> getCustomerAddresses(TAccountRequest accountRequest) {
    log.info("Getting addresses for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.getCustomerAddresses(accountRequest.id()));
    return ResponseEntity.ok("Customer addresses");
  }

  @PostMapping("/addresses")
  @ResponseMessage(message = "Address added successfully")
  @Operation(summary = "Add customer address", description = "Add a new address for customer")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> addCustomerAddress(
      @Valid @RequestBody Object addressRequest,
      TAccountRequest accountRequest) {
    log.info("Adding address for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.addCustomerAddress(addressRequest, accountRequest.id()));
    return ResponseEntity.ok("Address added");
  }

  // ================== WISHLIST MANAGEMENT ==================

  @GetMapping("/wishlist")
  @ResponseMessage(message = "Get wishlist success")
  @Operation(summary = "Get customer wishlist", description = "Get all items in customer's wishlist")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<PagedResponse<?>> getWishlist(
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Getting wishlist for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.getWishlist(accountRequest.id(), pageable));
    return ResponseEntity.ok(new PagedResponse<>(null, 0, 20, 0, 0, true));
  }

  @PostMapping("/wishlist/items")
  @ResponseMessage(message = "Item added to wishlist successfully")
  @Operation(summary = "Add to wishlist", description = "Add a product to customer's wishlist")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> addToWishlist(
      @Parameter(description = "Product ID", required = true) @RequestParam UUID productId,
      @Parameter(description = "Product variant ID") @RequestParam(required = false) UUID variantId,
      TAccountRequest accountRequest) {
    log.info("Adding product {} to wishlist for customer: {}", productId, accountRequest.id());
    // return ResponseEntity.ok(customerService.addToWishlist(productId, variantId, accountRequest.id()));
    return ResponseEntity.ok("Item added to wishlist");
  }

  @DeleteMapping("/wishlist/items/{itemId}")
  @ResponseMessage(message = "Item removed from wishlist successfully")
  @Operation(summary = "Remove from wishlist", description = "Remove an item from customer's wishlist")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<Void> removeFromWishlist(
      @Parameter(description = "Wishlist item ID", required = true) @PathVariable UUID itemId,
      TAccountRequest accountRequest) {
    log.info("Removing item {} from wishlist for customer: {}", itemId, accountRequest.id());
    // customerService.removeFromWishlist(itemId, accountRequest.id());
    return ResponseEntity.ok().build();
  }

  // ================== CUSTOMER PREFERENCES ==================

  @GetMapping("/preferences")
  @ResponseMessage(message = "Get customer preferences success")
  @Operation(summary = "Get customer preferences", description = "Get customer's shopping preferences and settings")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> getCustomerPreferences(TAccountRequest accountRequest) {
    log.info("Getting preferences for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.getCustomerPreferences(accountRequest.id()));
    return ResponseEntity.ok("Customer preferences");
  }

  @PutMapping("/preferences")
  @ResponseMessage(message = "Customer preferences updated successfully")
  @Operation(summary = "Update customer preferences", description = "Update customer's shopping preferences")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> updateCustomerPreferences(
      @Valid @RequestBody Object preferencesRequest,
      TAccountRequest accountRequest) {
    log.info("Updating preferences for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.updateCustomerPreferences(accountRequest.id(), preferencesRequest));
    return ResponseEntity.ok("Preferences updated");
  }

  // ================== CUSTOMER NOTIFICATIONS ==================

  @GetMapping("/notifications")
  @ResponseMessage(message = "Get customer notifications success")
  @Operation(summary = "Get customer notifications", description = "Get customer's notifications")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<PagedResponse<?>> getCustomerNotifications(
      @Parameter(description = "Read status filter") @RequestParam(required = false) Boolean isRead,
      @Parameter(description = "Notification type filter") @RequestParam(required = false) String type,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Getting notifications for customer: {}", accountRequest.id());
    // return ResponseEntity.ok(customerService.getCustomerNotifications(accountRequest.id(), isRead, type, pageable));
    return ResponseEntity.ok(new PagedResponse<>(null, 0, 20, 0, 0, true));
  }

  @PatchMapping("/notifications/{notificationId}/read")
  @ResponseMessage(message = "Notification marked as read")
  @Operation(summary = "Mark notification as read", description = "Mark a notification as read")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<?> markNotificationAsRead(
      @Parameter(description = "Notification ID", required = true) @PathVariable UUID notificationId,
      TAccountRequest accountRequest) {
    log.info("Marking notification {} as read for customer: {}", notificationId, accountRequest.id());
    // return ResponseEntity.ok(customerService.markNotificationAsRead(notificationId, accountRequest.id()));
    return ResponseEntity.ok("Notification marked as read");
  }
} 
