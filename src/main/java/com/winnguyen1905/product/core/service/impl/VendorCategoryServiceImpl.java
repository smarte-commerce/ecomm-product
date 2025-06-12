package com.winnguyen1905.product.core.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.mapper_v2.CategoryMapper;
import com.winnguyen1905.product.core.model.request.AddCateogryRequest;
import com.winnguyen1905.product.core.model.viewmodel.CategoryTreeVm;
import com.winnguyen1905.product.core.service.VendorCategoryService;
import com.winnguyen1905.product.persistance.entity.garbage.ECategory;
import com.winnguyen1905.product.persistance.repository.garbage.CategoryRepository;
import com.winnguyen1905.product.secure.TAccountRequest;
import com.winnguyen1905.product.util.ExtractorUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorCategoryServiceImpl implements VendorCategoryService {

  // private static final int NODE_SPACING = 2;
  // private final CategoryRepository categoryRepository;

  // @Override
  // public CategoryTreeVm findAllShopCategory(UUID shopId) {
  //   return CategoryMapper.toCategoryTree(categoryRepository.findAllByShopId(shopId));
  // }

  // @Override
  // public void addCategory(TAccountRequest accountRequest, AddCateogryRequest categoryDto) {
  //   ECategory newCategory = ECategory.builder()
  //       .name(categoryDto.name())
  //       .shopId(accountRequest.id())
  //       .description(categoryDto.description())
  //       .build();

  //   if (newCategory.getParentId() == null) {
  //     createRootCategory(accountRequest.id(), newCategory);
  //   } else {
  //     createChildCategory(accountRequest.id(), newCategory);
  //   }
  // }

  // private void createRootCategory(UUID shopId, ECategory category) {
  //   try {
  //     long count = categoryRepository.countByShopId(shopId);
  //     ECategory categoryWithPosition = calculateRootNodePositions(category, count);
  //     categoryRepository.save(categoryWithPosition);
  //   } catch (Exception e) {
  //     log.error("Error during creating root category: {}", e.getMessage());
  //     throw e;
  //   }
  // }

  // private void createChildCategory(UUID shopId, ECategory category) {
  //   try {
  //     ECategory parent = findParentCategory(shopId, category.getParentId());
  //     ECategory categoryWithPosition = calculateChildNodePositions(category, parent);
  //     categoryRepository.updateCategoryTreeOfShop(categoryWithPosition.getRight(), categoryWithPosition.getShopId());
  //     categoryRepository.save(categoryWithPosition);
  //   } catch (Exception e) {
  //     log.error("Error during creating child category: {}", e.getMessage());
  //     throw e;
  //   }
  // }

  // private ECategory calculateRootNodePositions(ECategory category, long count) {
  //   category.setLeft(count * NODE_SPACING + 1);
  //   category.setRight(count * NODE_SPACING + 2);
  //   return category;
  // }

  // private ECategory findParentCategory(UUID shopId, UUID parentId) {
  //   return ExtractorUtils.fromOptional(
  //       categoryRepository.findByIdAndShopId(parentId, shopId),
  //       String.format("Parent category not found: %s", parentId));
  // }

  // private ECategory calculateChildNodePositions(ECategory child, ECategory parent) {
  //   child.setLeft(parent.getRight());
  //   child.setRight(parent.getRight() + 1);
  //   return child;
  // }
}
