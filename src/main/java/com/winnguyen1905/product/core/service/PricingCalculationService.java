package com.winnguyen1905.product.core.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.core.model.request.PriceCalculationRequest;
import com.winnguyen1905.product.core.model.request.ReserveInventoryRequest;
import com.winnguyen1905.product.core.model.response.PriceCalculationResponse;
import com.winnguyen1905.product.core.model.response.ReserveInventoryResponse;
import com.winnguyen1905.product.exception.BadRequestException;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingCalculationService {

  private final ProductVariantRepository productVariantRepository;
  private final CustomerProductService customerProductService;

  /**
   * Calculate pricing for order items with inventory reservation
   */
  @Transactional
  public PriceCalculationResponse calculatePricing(PriceCalculationRequest request) {
    log.info("Calculating pricing for saga: {} with {} shops",
        request.getSagaId(), request.getShopItems().size());

    validateRequest(request);

    List<PriceCalculationResponse.ShopPricing> shopPricings = new ArrayList<>();
    boolean allInventoryReserved = true;
    boolean allDiscountsApplied = true;

    for (PriceCalculationRequest.ShopCheckoutItem shopItem : request.getShopItems()) {
      try {
        PriceCalculationResponse.ShopPricing shopPricing = calculateShopPricing(
            shopItem, request.getSagaId());
        shopPricings.add(shopPricing);

        // Check if all items in this shop had inventory reserved
        boolean shopInventoryReserved = shopPricing.getProductPricings().stream()
            .allMatch(PriceCalculationResponse.ShopPricing.ProductPricing::isInventoryReserved);
        allInventoryReserved = allInventoryReserved && shopInventoryReserved;

      } catch (Exception e) {
        log.error("Failed to calculate pricing for shop: {}", shopItem.getShopId(), e);
        allInventoryReserved = false;
        // Continue processing other shops but mark as failed
      }
    }

    return PriceCalculationResponse.builder()
        .shopPricings(shopPricings)
        .allInventoryReserved(allInventoryReserved)
        .allDiscountsApplied(allDiscountsApplied) // TODO: Implement discount logic
        .build();
  }

  private PriceCalculationResponse.ShopPricing calculateShopPricing(
      PriceCalculationRequest.ShopCheckoutItem shopItem, UUID orderId) {

    log.debug("Calculating pricing for shop: {} with {} items",
        shopItem.getShopId(), shopItem.getItems().size());

    List<PriceCalculationResponse.ShopPricing.ProductPricing> productPricings = new ArrayList<>();
    BigDecimal shopSubtotal = BigDecimal.ZERO;
    BigDecimal shopTaxAmount = BigDecimal.ZERO;

    for (PriceCalculationRequest.ShopCheckoutItem.ProductItem item : shopItem.getItems()) {
      PriceCalculationResponse.ShopPricing.ProductPricing productPricing = calculateProductPricing(item, orderId);
      productPricings.add(productPricing);

      shopSubtotal = shopSubtotal.add(BigDecimal.valueOf(productPricing.getLineTotal()));

      // Calculate tax (simplified - could be more complex based on tax category)
      BigDecimal itemTax = calculateTax(productPricing.getLineTotal(), item.getTaxCategory());
      shopTaxAmount = shopTaxAmount.add(itemTax);
    }

    return PriceCalculationResponse.ShopPricing.builder()
        .shopId(shopItem.getShopId())
        .subtotal(shopSubtotal.doubleValue())
        .taxAmount(shopTaxAmount.doubleValue())
        .productPricings(productPricings)
        .build();
  }

  private PriceCalculationResponse.ShopPricing.ProductPricing calculateProductPricing(
      PriceCalculationRequest.ShopCheckoutItem.ProductItem item, UUID orderId) {

    log.debug("Calculating pricing for product variant: {}", item.getVariantId());

    // Get product variant from database
    EProductVariant variant = productVariantRepository.findById(item.getVariantId())
        .orElseThrow(() -> new BadRequestException("Product variant not found: " + item.getVariantId()));

    // Get current price
    BigDecimal unitPrice = BigDecimal.valueOf(variant.getPrice());
    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

    // Reserve inventory
    boolean inventoryReserved = false;
    UUID reservationId = null;

    try {
      ReserveInventoryRequest reserveRequest = ReserveInventoryRequest.builder()
          .reservationId(UUID.randomUUID()) // Generate a new reservation ID
          .items(List.of(ReserveInventoryRequest.Item.builder()
              .variantId(item.getVariantId())
              .productId(item.getProductId())
              .quantity(item.getQuantity())
              .build()))
          .build();

      ReserveInventoryResponse reserveResponse = customerProductService.reserveInventory(reserveRequest);

      if (reserveResponse.isStatus() && reserveResponse.getItems() != null && !reserveResponse.getItems().isEmpty()) {
        inventoryReserved = true;
        reservationId = reserveResponse.getReservationId();
      }
    } catch (Exception e) {
      log.warn("Failed to reserve inventory for variant: {}, quantity: {}",
          item.getVariantId(), item.getQuantity(), e);
    }

    return PriceCalculationResponse.ShopPricing.ProductPricing.builder()
        .productId(item.getProductId())
        .variantId(item.getVariantId())
        .productSku(item.getProductSku())
        .quantity(item.getQuantity())
        .unitPrice(unitPrice.doubleValue())
        .lineTotal(lineTotal.doubleValue())
        .inventoryReserved(inventoryReserved)
        .reservationId(reservationId)
        .build();
  }

  private BigDecimal calculateTax(Double lineTotal, String taxCategory) {
    // Simplified tax calculation - could be more complex
    BigDecimal taxRate = BigDecimal.ZERO;

    if (taxCategory != null) {
      switch (taxCategory.toUpperCase()) {
        case "STANDARD":
          taxRate = new BigDecimal("0.10"); // 10%
          break;
        case "REDUCED":
          taxRate = new BigDecimal("0.05"); // 5%
          break;
        case "EXEMPT":
          taxRate = BigDecimal.ZERO;
          break;
        default:
          taxRate = new BigDecimal("0.10"); // Default 10%
      }
    }

    return BigDecimal.valueOf(lineTotal)
        .multiply(taxRate)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private void validateRequest(PriceCalculationRequest request) {
    if (request == null) {
      throw new BadRequestException("Price calculation request cannot be null");
    }

    if (request.getSagaId() == null) {
      throw new BadRequestException("Saga ID is required");
    }

    if (request.getCustomerId() == null) {
      throw new BadRequestException("Customer ID is required");
    }

    if (request.getShopItems() == null || request.getShopItems().isEmpty()) {
      throw new BadRequestException("Shop items are required");
    }

    for (PriceCalculationRequest.ShopCheckoutItem shopItem : request.getShopItems()) {
      if (shopItem.getShopId() == null) {
        throw new BadRequestException("Shop ID is required for all shop items");
      }

      if (shopItem.getItems() == null || shopItem.getItems().isEmpty()) {
        throw new BadRequestException("Items are required for shop: " + shopItem.getShopId());
      }

      for (PriceCalculationRequest.ShopCheckoutItem.ProductItem item : shopItem.getItems()) {
        if (item.getVariantId() == null) {
          throw new BadRequestException("Variant ID is required for all items");
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
          throw new BadRequestException("Valid quantity is required for variant: " + item.getVariantId());
        }
      }
    }
  }
}
