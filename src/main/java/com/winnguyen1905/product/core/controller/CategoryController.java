package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.service.VendorCategoryService;
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
 * Category Management REST API Controller
 * 
 * Provides endpoints for category creation, management, and retrieval
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Category Management", description = "APIs for category operations")
public class CategoryController {

  private final VendorCategoryService categoryService;
  
  @GetMapping
  @Operation(summary = "Get all categories", description = "Retrieves all categories with pagination and hierarchical structure")
  public ResponseEntity<?> getAllCategories(
      @Parameter(description = "Include inactive categories") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
    log.info("Getting all categories, includeInactive: {}", includeInactive);
    return ResponseEntity.ok(categoryService.getAllCategories(includeInactive));
  }
  
  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Retrieves category details by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<?> getCategoryById(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id) {
    log.info("Getting category with ID: {}", id);
    return ResponseEntity.ok(categoryService.getCategoryById(id));
  }
  
  @PostMapping
  @ResponseMessage(message = "Create category success")
  @Operation(summary = "Create category", description = "Creates a new category")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Category created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> createCategory(
      @Valid @RequestBody Object categoryRequest,
      TAccountRequest accountRequest) {
    log.info("Creating category by admin: {}", accountRequest.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(categoryService.createCategory(categoryRequest, accountRequest));
  }
  
  @PutMapping("/{id}")
  @ResponseMessage(message = "Update category success")
  @Operation(summary = "Update category", description = "Updates an existing category")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> updateCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody Object categoryRequest,
      TAccountRequest accountRequest) {
    log.info("Updating category: {} by admin: {}", id, accountRequest.id());
    return ResponseEntity.ok(categoryService.updateCategory(id, categoryRequest, accountRequest));
  }
  
  @DeleteMapping("/{id}")
  @ResponseMessage(message = "Delete category success")
  @Operation(summary = "Delete category", description = "Deletes an existing category")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Category has associated products"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      TAccountRequest accountRequest) {
    log.info("Deleting category: {} by admin: {}", id, accountRequest.id());
    categoryService.deleteCategory(id, accountRequest);
    return ResponseEntity.ok().build();
  }
  
  @GetMapping("/tree")
  @Operation(summary = "Get category tree", description = "Retrieves hierarchical category tree")
  public ResponseEntity<?> getCategoryTree() {
    log.info("Getting category tree");
    return ResponseEntity.ok(categoryService.getCategoryTree());
  }
  
  @PostMapping("/{id}/move")
  @ResponseMessage(message = "Move category success")
  @Operation(summary = "Move category", description = "Moves category to a new parent")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> moveCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      @Parameter(description = "New parent category ID") @RequestParam(required = false) UUID parentId,
      TAccountRequest accountRequest) {
    log.info("Moving category: {} to parent: {} by admin: {}", id, parentId, accountRequest.id());
    return ResponseEntity.ok(categoryService.moveCategory(id, parentId, accountRequest));
  }
  
  @GetMapping("/search")
  @Operation(summary = "Search categories", description = "Searches categories by name or other criteria")
  public ResponseEntity<?> searchCategories(
      @Parameter(description = "Search query") @RequestParam String query,
      @PageableDefault(size = 20) Pageable pageable) {
    log.info("Searching categories with query: {}", query);
    return ResponseEntity.ok(categoryService.searchCategories(query, pageable));
  }
  
  @GetMapping("/{id}/products")
  @Operation(summary = "Get category products", description = "Retrieves products belonging to a category")
  public ResponseEntity<?> getCategoryProducts(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      @PageableDefault(size = 20) Pageable pageable) {
    log.info("Getting products for category: {}", id);
    return ResponseEntity.ok(categoryService.getCategoryProducts(id, pageable));
  }
}
