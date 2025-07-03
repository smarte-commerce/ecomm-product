package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.service.BrandService;
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

import java.util.UUID;

/**
 * Brand Management REST API Controller
 * 
 * Provides endpoints for brand creation, management, and retrieval
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Validated
@Tag(name = "Brand Management", description = "APIs for brand operations")
public class BrandController {
  
  private final BrandService brandService;
  
  @GetMapping
  @Operation(summary = "Get all brands", description = "Retrieves all brands with pagination")
  public ResponseEntity<?> getAllBrands(
      @PageableDefault(size = 20) Pageable pageable) {
    log.info("Getting all brands with pagination");
    return ResponseEntity.ok(brandService.getAllBrands(pageable));
  }
  
  @GetMapping("/{id}")
  @Operation(summary = "Get brand by ID", description = "Retrieves brand details by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved brand"),
    @ApiResponse(responseCode = "404", description = "Brand not found")
  })
  public ResponseEntity<?> getBrandById(
      @Parameter(description = "Brand ID", required = true) @PathVariable UUID id) {
    log.info("Getting brand with ID: {}", id);
    return ResponseEntity.ok(brandService.getBrandById(id));
  }
  
  @PostMapping
  @ResponseMessage(message = "Create brand success")
  @Operation(summary = "Create brand", description = "Creates a new brand")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Brand created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
  public ResponseEntity<?> createBrand(
      @Valid @RequestBody Object brandRequest,
      TAccountRequest accountRequest) {
    log.info("Creating brand by user: {}", accountRequest.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(brandService.createBrand(brandRequest, accountRequest));
  }
  
  @PutMapping("/{id}")
  @ResponseMessage(message = "Update brand success")
  @Operation(summary = "Update brand", description = "Updates an existing brand")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "404", description = "Brand not found")
  })
  @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
  public ResponseEntity<?> updateBrand(
      @Parameter(description = "Brand ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody Object brandRequest,
      TAccountRequest accountRequest) {
    log.info("Updating brand: {} by user: {}", id, accountRequest.id());
    return ResponseEntity.ok(brandService.updateBrand(id, brandRequest, accountRequest));
  }
  
  @DeleteMapping("/{id}")
  @ResponseMessage(message = "Delete brand success")
  @Operation(summary = "Delete brand", description = "Deletes an existing brand")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Brand deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Brand not found")
  })
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteBrand(
      @Parameter(description = "Brand ID", required = true) @PathVariable UUID id,
      TAccountRequest accountRequest) {
    log.info("Deleting brand: {} by admin: {}", id, accountRequest.id());
    brandService.deleteBrand(id, accountRequest);
    return ResponseEntity.ok().build();
  }
  
  @GetMapping("/vendor/{vendorId}")
  @Operation(summary = "Get vendor brands", description = "Retrieves all brands for a specific vendor")
  @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
  public ResponseEntity<?> getVendorBrands(
      @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId,
      @PageableDefault(size = 20) Pageable pageable,
      TAccountRequest accountRequest) {
    log.info("Getting brands for vendor: {} by user: {}", vendorId, accountRequest.id());
    return ResponseEntity.ok(brandService.getVendorBrands(vendorId, pageable, accountRequest));
  }
  
  @GetMapping("/search")
  @Operation(summary = "Search brands", description = "Searches brands by name or other criteria")
  public ResponseEntity<?> searchBrands(
      @Parameter(description = "Search query") @RequestParam String query,
      @PageableDefault(size = 20) Pageable pageable) {
    log.info("Searching brands with query: {}", query);
    return ResponseEntity.ok(brandService.searchBrands(query, pageable));
  }
}
