package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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

public interface CustomerProductService {
  ProductDetailVm getProductDetail(UUID id);

  List<ProductVariantDetailVm> getProductVariantDetails(UUID productId);

  // To see the product in Cart page
  ProductVariantByShopVm getProductVariantDetails(Set<UUID> productVariantIds);

  // @Cacheable(value = "productSearch", key = "#request")
  PagedResponse<ProductVariantReviewVm> searchProducts(SearchProductRequest request);

  ProductAvailabilityResponse checkProductAvailability(ProductAvailabilityRequest productAvailabilityRequest);

  ReserveInventoryResponse reserveInventory(ReserveInventoryRequest reserveInventoryRequest);

  InventoryConfirmationResponse inventoryConfirmation(InventoryConfirmationRequest inventoryConfirmationRequest);
}
