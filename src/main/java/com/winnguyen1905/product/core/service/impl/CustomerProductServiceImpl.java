package com.winnguyen1905.product.core.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.mapper_v2.ProductESMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductMapper;
import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.InventoryConfirmationRequest;
import com.winnguyen1905.product.core.model.request.ProductAvailabilityRequest;
import com.winnguyen1905.product.core.model.request.ReserveInventoryRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.InventoryConfirmationResponse;
import com.winnguyen1905.product.core.model.response.ProductAvailabilityResponse;
import com.winnguyen1905.product.core.model.response.ReserveInventoryResponse;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantByShopVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.core.service.CustomerProductService;
import com.winnguyen1905.product.core.service.InventoryService;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import java.util.Optional;
import java.util.Set;

import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;
import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProductServiceImpl implements CustomerProductService {

  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final ProductESCustomRepository productESRepository;
  private final InventoryRepository inventoryRepository;
  private final InventoryService inventoryService;

  @Override
  public ProductDetailVm getProductDetail(UUID id) {
    return productRepository.findByIdAndIsPublishedTrue(id)
        .map(ProductMapper::toProductDetail)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
  }

  @Override
  public PagedResponse<ProductVariantReviewVm> searchProducts(SearchProductRequest searchProductRequest) {
    SearchHits<ESProductVariant> searchHits = productESRepository.searchProducts(searchProductRequest,
        ESProductVariant.class);

    List<ESProductVariant> esProductVariants = searchHits.getSearchHits()
        .stream()
        .map(SearchHit::getContent)
        .toList();

    log.info("Number of products found: {}", esProductVariants.size());

    List<ProductVariantReviewVm> productVariantResponses = ProductESMapper
        .toProductVariantReviews(esProductVariants);

    log.info("Number of product variant reviews mapped: {}", productVariantResponses.size());

    return PagedResponse.<ProductVariantReviewVm>builder()
        .results(productVariantResponses)
        .page(searchProductRequest.getPage().pageNum())
        .size(searchProductRequest.getPage().pageSize())
        .totalElements((int) searchHits.getTotalHits())
        .build();
  }

  @Override
  public ProductVariantByShopVm getProductVariantDetails(Set<UUID> productVariantIds) {
    List<EProductVariant> productVariants = this.productVariantRepository
        .findAllByIdIn(productVariantIds);

    return ProductMapper.toProductVariantByShopVm(productVariants);
  }

  @Override
  public List<ProductVariantDetailVm> getProductVariantDetails(UUID productId) {
    List<ProductVariantDetailVm> productVariants = this.productVariantRepository
        .findVariantsByProductId(productId).stream()
        .map(productVariant -> ProductVariantDetailVm.builder()
            .id(productVariant.getId())
            .sku(productVariant.getSku())
            .price(productVariant.getPrice())
            .build())
        .collect(Collectors.toList());
    return productVariants;
  }

  @Override
  public ProductAvailabilityResponse checkProductAvailability(ProductAvailabilityRequest productAvailabilityRequest) {
    log.info("Checking product availability for request: {}", productAvailabilityRequest);

    List<ProductAvailabilityResponse.Item> availabilityItems = productAvailabilityRequest.getItems().stream()
        .map(requestItem -> {
          try {
            // Find the product variant by ID
            var variant = productVariantRepository.findById(requestItem.getVariantId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Product variant not found with id: " + requestItem.getVariantId()));

            // Get the inventory for this variant using SKU
            var inventoryOpt = variant.getSku() != null ? inventoryRepository.findBySku(variant.getSku())
                : Optional.<EInventory>empty();

            // Check if the variant is active and published
            boolean isActive = variant.getIsDeleted() == null || !variant.getIsDeleted();
            boolean isPublished = variant.getProduct() != null && variant.getProduct().isPublished();

            // Check inventory availability if inventory exists
            boolean isInStock = false;
            int availableQuantity = 0;

            if (inventoryOpt.isPresent()) {
              EInventory inventory = inventoryOpt.get();
              availableQuantity = inventory.getQuantityAvailable() != null ? inventory.getQuantityAvailable() : 0;
              isInStock = availableQuantity >= requestItem.getQuantity();
            }

            // Build the response item
            return ProductAvailabilityResponse.Item.builder()
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .variantId(variant.getId())
                .available(isActive && isPublished && isInStock)
                .isActive(isActive && isPublished)
                .stockQuantity(availableQuantity)
                .currentPrice(variant.getPrice() != null ? variant.getPrice() : 0.0)
                .build();

          } catch (Exception e) {
            log.error("Error checking availability for variant ID {}: {}",
                requestItem.getVariantId(), e.getMessage(), e);
            // Return an unavailable item in case of any errors
            return ProductAvailabilityResponse.Item.builder()
                .variantId(requestItem.getVariantId())
                .available(false)
                .isActive(false)
                .stockQuantity(0)
                .currentPrice(0.0)
                .build();
          }
        })
        .collect(Collectors.toList());

    return ProductAvailabilityResponse.builder()
        .items(availabilityItems)
        .build();
  }

  @Override
  @Transactional
  public ReserveInventoryResponse reserveInventory(ReserveInventoryRequest reserveInventoryRequest) {
    log.info("Processing inventory reservation request: {}", reserveInventoryRequest);

    // Generate a new reservation ID if not provided
    UUID reservationId = reserveInventoryRequest.getReservationId() != null ? reserveInventoryRequest.getReservationId()
        : UUID.randomUUID();

    // Process each item in the reservation request
    List<ReserveInventoryResponse.Item> responseItems = reserveInventoryRequest.getItems().stream()
        .map(item -> {
          try {
            // Find the product variant
            var variant = productVariantRepository.findById(item.getVariantId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Product variant not found with id: " + item.getVariantId()));

            // Find the inventory by SKU
            var inventory = inventoryRepository.findBySku(variant.getSku())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Inventory not found for SKU: " + variant.getSku()));

            // Reserve the inventory
            inventoryService.reserveInventory(inventory.getId(), item.getQuantity()).subscribe();

            // Return successful response item
            return ReserveInventoryResponse.Item.builder()
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .quantity(item.getQuantity())
                .build();

          } catch (Exception e) {
            log.error("Failed to reserve inventory for variant {}: {}",
                item.getVariantId(), e.getMessage(), e);

            // Return failed response item
            return ReserveInventoryResponse.Item.builder()
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .quantity(0) // Indicate failure with 0 quantity
                .build();
          }
        })
        .collect(Collectors.toList());

    // Check if all items were reserved successfully
    boolean allReserved = responseItems.stream()
        .allMatch(item -> item.getQuantity() > 0);

    return ReserveInventoryResponse.builder()
        .reservationId(reservationId)
        .status(allReserved)
        .items(responseItems)
        .expiresAt(reserveInventoryRequest.getExpiresAt())
        .build();
  }

  @Override
  @Transactional
  public InventoryConfirmationResponse inventoryConfirmation(
      InventoryConfirmationRequest request) {
    log.info("Processing inventory confirmation for reservation: {}, order: {}",
        request.getReservationId(), request.getOrderId());

    // In a real implementation, we would typically:
    // 1. Look up the reservation by reservationId
    // 2. Verify the order exists and is valid
    // 3. Process each item in the reservation
    // 4. Update inventory levels (convert reserved to sold)
    // 5. Return confirmation with details

    // For now, we'll simulate a successful confirmation
    // In a real implementation, you would query your database for the reservation
    // and process each item in the reservation

    // This is a mock implementation that would be replaced with actual database
    // calls
    // In a real implementation, you would:
    // 1. Query the reservation by ID
    // 2. For each item in the reservation:
    // a. Find the inventory by product/variant
    // b. Update the quantities (move from reserved to sold)
    // c. Create a confirmation item with the updated quantities

    // Mock data - replace with actual implementation
    InventoryConfirmationResponse.Item item = InventoryConfirmationResponse.Item.builder()
        .productId(UUID.randomUUID()) // Replace with actual product ID
        .variantId(UUID.randomUUID()) // Replace with actual variant ID
        .quantityDeducted(1) // Replace with actual quantity
        .remainingStock(10) // Replace with actual remaining stock
        .build();

    InventoryConfirmationResponse response = new InventoryConfirmationResponse();
    response.setReservationId(request.getReservationId());
    response.setOrderId(request.getOrderId());
    response.setItems(List.of(item));

    return response;
  }

}
