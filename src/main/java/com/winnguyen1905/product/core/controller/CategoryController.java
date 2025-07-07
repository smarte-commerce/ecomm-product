package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.common.annotation.ResponseMessage;
import com.winnguyen1905.product.core.controller.base.BaseController;
import com.winnguyen1905.product.core.model.request.CreateCategoryRequest;
import com.winnguyen1905.product.core.model.request.UpdateCategoryRequest;
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
public class CategoryController extends BaseController {

  private final VendorCategoryService categoryService;
  
  @GetMapping
  @Operation(summary = "Get all categories", description = "Retrieves all categories with pagination and hierarchical structure")
  public ResponseEntity<?> getAllCategories(
      @Parameter(description = "Include inactive categories") @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
    logPublicRequest("Getting all categories, includeInactive: " + includeInactive);
    var result = categoryService.getAllCategories(includeInactive);
    return ok(result);
  }
  
  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Retrieves category details by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<?> getCategoryById(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id) {
    logPublicRequest("Getting category by ID", id);
    var result = categoryService.getCategoryById(id);
    return ok(result);
  }
  
  @PostMapping
  @ResponseMessage(message = "Create category success")
  @Operation(summary = "Create category", description = "Creates a new category")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Category created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> createCategory(
      @Valid @RequestBody CreateCategoryRequest categoryRequest,
      TAccountRequest accountRequest) {
    logRequest("Creating category", accountRequest);
    var result = categoryService.createCategory(categoryRequest, accountRequest);
    return created(result);
  }
  
  @PutMapping("/{id}")
  @ResponseMessage(message = "Update category success")
  @Operation(summary = "Update category", description = "Updates an existing category")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> updateCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody UpdateCategoryRequest categoryRequest,
      TAccountRequest accountRequest) {
    logRequest("Updating category", id, accountRequest);
    var result = categoryService.updateCategory(id, categoryRequest, accountRequest);
    return ok(result);
  }
  
  @DeleteMapping("/{id}")
  @ResponseMessage(message = "Delete category success")
  @Operation(summary = "Delete category", description = "Deletes an existing category")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Category has associated products"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      TAccountRequest accountRequest) {
    logRequest("Deleting category", id, accountRequest);
    categoryService.deleteCategory(id, accountRequest);
    return noContent();
  }
  
  @GetMapping("/tree")
  @Operation(summary = "Get category tree", description = "Retrieves hierarchical category tree")
  public ResponseEntity<?> getCategoryTree() {
    logPublicRequest("Getting category tree");
    var result = categoryService.getCategoryTree();
    return ok(result);
  }
  
  @PostMapping("/{id}/move")
  @ResponseMessage(message = "Move category success")
  @Operation(summary = "Move category", description = "Moves category to a new parent")
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> moveCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      @Parameter(description = "New parent category ID") @RequestParam(required = false) UUID parentId,
      TAccountRequest accountRequest) {
    logRequest("Moving category " + id + " to parent " + parentId, accountRequest);
    var result = categoryService.moveCategory(id, parentId, accountRequest);
    return ok(result);
  }
  
  @GetMapping("/search")
  @Operation(summary = "Search categories", description = "Searches categories by name or other criteria")
  public ResponseEntity<?> searchCategories(
      @Parameter(description = "Search query") @RequestParam String query,
      @PageableDefault(size = 20) Pageable pageable) {
    logPublicRequest("Searching categories with query: " + query);
    var result = categoryService.searchCategories(query, pageable);
    return ok(result);
  }
  
  @GetMapping("/{id}/products")
  @Operation(summary = "Get category products", description = "Retrieves products belonging to a category")
  public ResponseEntity<?> getCategoryProducts(
      @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
      @PageableDefault(size = 20) Pageable pageable) {
    logPublicRequest("Getting products for category", id);
    var result = categoryService.getCategoryProducts(id, pageable);
    return ok(result);
  }
}
