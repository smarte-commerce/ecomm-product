package com.winnguyen1905.product.core.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.product.common.SystemConstant;
import com.winnguyen1905.product.config.SecurityUtils;
import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.service.ProductService;
import com.winnguyen1905.product.util.MetaMessage;
import com.winnguyen1905.product.util.OptionalExtractor;

@RestController
@RequiredArgsConstructor
@RequestMapping("products")
public class ProductController {

  private final ProductService productService;

  // PUBLIC API----------------------------------------------------------------

  // @GetMapping("/")
  // @MetaMessage(message = "Get all product with filter success")
  // public ResponseEntity<Product> getAllProducts(
  //     Pageable pageable,
  //     @ModelAttribute(SystemConstant.MODEL) SearchProductRequest productSearchRequest) {
  //   productSearchRequest.setIsDraft(false);
  //   productSearchRequest.setIsPublished(true);
  //   return ResponseEntity.ok(this.productService.handle(productSearchRequest,
  //       pageable));
  // }

  @GetMapping("/{id}")
  @MetaMessage(message = "get product with by id success")
  public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
    return ResponseEntity.ok(this.productService.handleGetProduct(id));
  }

  // API FOR SHOP OWNER---------------------------------------------------------

  @PostMapping
  @MetaMessage(message = "add new product success")
  public ResponseEntity<Product> addProduct(@RequestBody AddProductRequest productRequest) {
    UUID userId = OptionalExtractor.currentUserId();
    return ResponseEntity.status(HttpStatus.CREATED.value())
        .body(this.productService.handleAddProduct(userId, productRequest));
  }

  // @GetMapping("/my-product")
  // @MetaMessage(message = "get all my product with filter success")
  // public ResponseEntity<Product> getAllMyProducts(
  //     Pageable pageable,
  //     @ModelAttribute(SystemConstant.MODEL) SearchProductRequest productSearchRequest) {
  //   UUID shopOwner = OptionalExtractor.currentUserId();
  //   productSearchRequest.setCreatedBy(shopOwner);
  //   return ResponseEntity.ok(this.productService.handleGetAllProducts(productSearchRequest,
  //       pageable));
  // }

  // @PatchMapping
  // @MetaMessage(message = "get all my product with filter success")
  // public ResponseEntity<List<Product>> updateProducts(@RequestBody List<AddProductRequest> productRequests) {
  //   UUID userId = SecurityUtils.getCurrentUserId()
  //       .orElseThrow(() -> new CustomRuntimeException("Not found userId", 403));
  //   return ResponseEntity.ok(this.productService.handleUpdateManyProducts(productRequests,
  //       userId));
  // }

  @PatchMapping("/change-status/{ids}")
  @MetaMessage(message = "Change visible products status success")
  public ResponseEntity<List<Product>> publishProducts(@PathVariable List<UUID> ids) {
    UUID shopId = OptionalExtractor.currentUserId();
    return ResponseEntity.ok(this.productService.handleChangeProductStatus(shopId, ids));
  }

  @DeleteMapping("/{ids}")
  public ResponseEntity<Void> deleteProducts(@PathVariable Set<UUID> ids) {
    UUID shopId = OptionalExtractor.currentUserId();
    this.productService.handleDeleteProducts(shopId, List.copyOf(ids));
    return ResponseEntity.noContent().build();
  }

  // API FOR SHOP ADMIN---------------------------------------------------------
}
