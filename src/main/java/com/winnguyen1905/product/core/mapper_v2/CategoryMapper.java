package com.winnguyen1905.product.core.mapper_v2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.winnguyen1905.product.core.model.request.AddCateogryRequest;
import com.winnguyen1905.product.core.model.viewmodel.CategoryTreeVm;
import com.winnguyen1905.product.persistance.entity.ECategory;

public class CategoryMapper {

  /**
   * Convert list of categories to hierarchical tree structure
   * Uses the existing CategoryTreeVm structure with CategoryItem and categoryChilds
   */
  public static CategoryTreeVm toCategoryTree(List<ECategory> categories) {
    if (categories == null || categories.isEmpty()) {
      return CategoryTreeVm.builder()
          .categoryItem(null)
          .categoryChilds(new ArrayList<>())
          .build();
    }

    // Find root category (first one or one without parent)
    ECategory rootCategory = categories.stream()
        .filter(cat -> cat.getParent() == null)
        .findFirst()
        .orElse(categories.get(0));

    // Get children of root category
    List<CategoryTreeVm.CategoryItem> children = categories.stream()
        .filter(cat -> cat.getParent() != null &&
                      cat.getParent().getId().equals(rootCategory.getId()))
        .map(CategoryMapper::toCategoryItem)
        .collect(Collectors.toList());

    return CategoryTreeVm.builder()
        .categoryItem(toCategoryItem(rootCategory))
        .categoryChilds(children)
        .build();
  }

  /**
   * Convert ECategory to CategoryItem
   */
  public static CategoryTreeVm.CategoryItem toCategoryItem(ECategory category) {
    if (category == null) return null;

    return new CategoryTreeVm.CategoryItem(
        category.getId(),
        category.getName(),
        category.getDescription()
    );
  }

  /**
   * Convert category request to entity with enhanced fields
   */
  public static ECategory toCategoryEntity(AddCateogryRequest request) {
    if (request == null) return null;

    ECategory.ECategoryBuilder builder = ECategory.builder()
        .name(request.name())
        .description(request.description())
        .isPublished(request.isPublished() != null ? request.isPublished() : true);

    // Handle parent relationship if parentId is provided
    if (request.parentId() != null && !request.parentId().trim().isEmpty()) {
      try {
        UUID.fromString(request.parentId());
        // Note: Parent entity should be set by the service layer
        // builder.parent(parentCategory);
      } catch (IllegalArgumentException e) {
        // Invalid UUID format, ignore parent
      }
    }

    // Handle shop/vendor ID if provided
    if (request.shopId() != null && !request.shopId().trim().isEmpty()) {
      try {
        UUID shopUuid = UUID.fromString(request.shopId());
        builder.vendorId(shopUuid);
      } catch (IllegalArgumentException e) {
        // Invalid UUID format, ignore
      }
    }

    return builder.build();
  }

  /**
   * Convert flat list to CategoryItem list
   */
  public static List<CategoryTreeVm.CategoryItem> toCategoryItemList(List<ECategory> categories) {
    if (categories == null) return new ArrayList<>();

    return categories.stream()
        .map(CategoryMapper::toCategoryItem)
        .collect(Collectors.toList());
  }

  /**
   * Update existing category entity with request data
   */
  public static void updateCategoryFromRequest(ECategory category, AddCateogryRequest request) {
    if (category == null || request == null) return;

    if (request.name() != null) category.setName(request.name());
    if (request.description() != null) category.setDescription(request.description());
    if (request.isPublished() != null) category.setIsPublished(request.isPublished());

    // Handle parent relationship update if parentId is provided
    if (request.parentId() != null && !request.parentId().trim().isEmpty()) {
      try {
        UUID.fromString(request.parentId());
        // Note: Parent entity should be set by the service layer
        // category.setParent(parentCategory);
      } catch (IllegalArgumentException e) {
        // Invalid UUID format, ignore parent update
      }
    }
  }

  /**
   * Create a simple category tree for a single category with its immediate children
   */
  public static CategoryTreeVm createSimpleTree(ECategory parent, List<ECategory> children) {
    if (parent == null) {
      return CategoryTreeVm.builder()
          .categoryItem(null)
          .categoryChilds(new ArrayList<>())
          .build();
    }

    List<CategoryTreeVm.CategoryItem> childItems = children != null ?
        children.stream()
            .map(CategoryMapper::toCategoryItem)
            .collect(Collectors.toList()) :
        new ArrayList<>();

    return CategoryTreeVm.builder()
        .categoryItem(toCategoryItem(parent))
        .categoryChilds(childItems)
        .build();
  }
}
