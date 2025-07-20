package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.PriceCalculationRequest;
import com.winnguyen1905.product.core.model.response.PriceCalculationResponse;
import com.winnguyen1905.product.core.service.PricingCalculationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Internal Service REST API Controller
 * 
 * Internal endpoints for service-to-service communication
 * Handles pricing calculations, bulk operations, and internal workflows
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@Validated
@Tag(name = "Internal Services", description = "Internal APIs for service-to-service communication")
public class InternalController extends BaseController {

  private final PricingCalculationService pricingCalculationService;

  // ================== PRICING OPERATIONS ==================

  @PostMapping("/calculate-pricing")
  @ResponseMessage(message = "Calculate pricing success")
  @Operation(summary = "Calculate order pricing", description = "Calculate pricing for order items with inventory reservation")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully calculated pricing"),
      @ApiResponse(responseCode = "400", description = "Invalid pricing request"),
      @ApiResponse(responseCode = "404", description = "Product variants not found"),
      @ApiResponse(responseCode = "409", description = "Insufficient inventory")
  })
  public ResponseEntity<PriceCalculationResponse> calculatePricing(
      @Valid @RequestBody PriceCalculationRequest request) {

    log.info("Processing pricing calculation for saga: {} with {} shops",
        request.getSagaId(), request.getShopItems().size());

    PriceCalculationResponse response = pricingCalculationService.calculatePricing(request);
    return ok(response);
  }

  // ================== HEALTH CHECK ==================

  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Check service health for internal monitoring")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Service is healthy")
  })
  public ResponseEntity<String> healthCheck() {
    log.debug("Internal health check requested");
    return ok("Product service is healthy");
  }
} 
