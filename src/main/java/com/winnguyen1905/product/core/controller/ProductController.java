package com.winnguyen1905.product.core.controller;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.common.constant.SystemConstant;
import com.winnguyen1905.product.core.model.ProductVariantDetailVm;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantByShopVm;
import com.winnguyen1905.product.core.model.viewmodel.ProductVariantReviewVm;
import com.winnguyen1905.product.core.service.CustomerProductService;
import com.winnguyen1905.product.core.service.VendorProductService;
import com.winnguyen1905.product.secure.AccountRequest;
import com.winnguyen1905.product.secure.TAccountRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/products")
public class ProductController {

  private final VendorProductService vendorProductService;
  private final CustomerProductService customerProductService;

  // PUBLIC API----------------------------------------------------------------

  @GetMapping("/search")
  @ResponseMessage(message = "Get all product with filter success")
  public ResponseEntity<PagedResponse<ProductVariantReviewVm>> searchProducts(
      @RequestBody SearchProductRequest productSearchRequest) {
    return ResponseEntity.ok(this.customerProductService.searchProducts(productSearchRequest));
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "get product with by id success")
  public ResponseEntity<ProductDetailVm> getProductById(@PathVariable UUID id) {
    return ResponseEntity.ok(this.customerProductService.getProductDetail(id));
  }

  @GetMapping("/{productId}/images")
  @ResponseMessage(message = "Get product images success")
  public ResponseEntity<PagedResponse<ProductImageVm>> getProductImages(
      @PathVariable UUID productId,
      Pageable pageable) {
    return ResponseEntity.ok(null);
  }

  @GetMapping("/{productId}/variants")
  @ResponseMessage(message = "Get product variations success")
  public ResponseEntity<List<ProductVariantDetailVm>> getProductVariations(
      @PathVariable UUID productId,
      Pageable pageable) {
    return ResponseEntity.ok(customerProductService.getProductVariantDetails(productId));
  }

  // Vendor API-------------------------------------------------------------

  @PostMapping("/create")
  @ResponseMessage(message = "Add new product success")
  public ResponseEntity<Void> addProduct(@RequestBody AddProductRequest productRequest,
      @AccountRequest TAccountRequest accountRequest) {
    this.vendorProductService.addProduct(accountRequest, productRequest);
    return ResponseEntity.status(HttpStatus.CREATED.value()).build();
  }

  @PostMapping("/update")
  @ResponseMessage(message = "Update product success")
  public ResponseEntity<Void> updateProduct(@RequestBody UpdateProductRequest updateProductRequest,
      @AccountRequest TAccountRequest accountRequest) {
    this.vendorProductService.updateProduct(accountRequest, updateProductRequest);
    return ResponseEntity.status(HttpStatus.OK.value()).build();
  }

  @PostMapping("/{id}/delete")
  public String postMethodName(@RequestBody String entity) {
    return entity;
  }

  // @GetMapping("/variant-details/{ids}")
  // public ResponseEntity<ProductVariantByShopVm>
  // getProductVariantDetail(@PathVariable List<String> ids) {
  // return
  // ResponseEntity.ok(this.customerProductService.getProductVariantDetails(ids));
  // }

  // @GetMapping("/my-product")
  // @MetaMessage(message = "get all my product with filter success")
  // public ResponseEntity<Product> getAllMyProducts(
  // Pageable pageable,
  // @ModelAttribute(SystemConstant.MODEL) SearchProductRequest
  // productSearchRequest) {
  // UUID shopOwner = OptionalExtractor.currentUserId();
  // productSearchRequest.setCreatedBy(shopOwner);
  // return
  // ResponseEntity.ok(this.productService.handleGetAllProducts(productSearchRequest,
  // pageable));
  // }

  // @PatchMapping
  // @MetaMessage(message = "get all my product with filter success")
  // public ResponseEntity<List<Product>> updateProducts(@RequestBody
  // List<AddProductRequest> productRequests) {
  // UUID userId = SecurityUtils.getCurrentUserId()
  // .orElseThrow(() -> new CustomRuntimeException("Not found userId", 403));
  // return
  // ResponseEntity.ok(this.productService.handleUpdateManyProducts(productRequests,
  // userId));
  // }

  // @PatchMapping("/change-status/{ids}")
  // @MetaMessage(message = "Change visible products status success")
  // public ResponseEntity<List<Product>> publishProducts(@PathVariable List<UUID>
  // ids) {
  // UUID shopId = OptionalExtractor.currentUserId();
  // return
  // ResponseEntity.ok(this.productService.handleChangeProductStatus(shopId,
  // ids));
  // }

  // @DeleteMapping("/{ids}")
  // public ResponseEntity<Void> deleteProducts(@PathVariable Set<UUID> ids) {
  // UUID shopId = OptionalExtractor.currentUserId();
  // this.productService.handleDeleteProducts(shopId, List.copyOf(ids));
  // return ResponseEntity.noContent().build();
  // }

  // API FOR SHOP ADMIN---------------------------------------------------------
}
