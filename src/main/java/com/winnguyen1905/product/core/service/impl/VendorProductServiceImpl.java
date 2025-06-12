package com.winnguyen1905.product.core.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.core.AbstractElasticsearchTemplate;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.core.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.secure.TAccountRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnguyen1905.product.core.mapper_v2.InventoryMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductESMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductImageMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductMapper;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.viewmodel.ProductDetailVm;
import com.winnguyen1905.product.core.service.VendorProductService;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.entity.garbage.EBrand;
import com.winnguyen1905.product.persistance.entity.garbage.ECategory;
import com.winnguyen1905.product.persistance.entity.garbage.EProductImage;
import com.winnguyen1905.product.persistance.repository.ProductESRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;
import com.winnguyen1905.product.persistance.repository.garbage.BrandRepository;
import com.winnguyen1905.product.persistance.repository.garbage.CategoryRepository;
import com.winnguyen1905.product.util.CommonUtils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorProductServiceImpl implements VendorProductService {

  private final BrandRepository brandRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ElasticsearchClient elasticsearchClient;
  private final ProductESCustomRepository productESRepository;

  @Override
  @Transactional
  public void addProduct(TAccountRequest accountRequest, AddProductRequest request) {
    // List<EProductImage> images = CommonUtils.stream(request.images())
    // .map(ProductImageMapper::toProductImageEntity)
    // .toList();
    List<EProductVariant> variants = CommonUtils.stream(request.variations())
        .map(ProductMapper::toProductVariantEntity)
        .toList();
    List<EInventory> inventories = CommonUtils.stream(request.inventories())
        .map(InventoryMapper::toInventoryEntity)
        .toList();

    // EBrand brand = brandRepository.findByCode(request.brandCode())
    // .orElseThrow(() -> new EntityNotFoundException("Not found brand"));

    // ECategory category =
    // categoryRepository.findByCodeAndShopId(request.categoryCode(),
    // accountRequest.id())
    // .orElseThrow(() -> new EntityNotFoundException("Not found category"));

    EProduct product = EProduct.builder()
        .name(request.name())
        .region(request.region())
        .description(request.description())
        .features(request.features())
        .isPublished(false)
        // .brand(brand)
        .shopId(accountRequest.id())
        .productType(request.type())
        // .category(category)
        // .images(images)
        .variations(variants)
        .inventories(inventories)
        .build();

    // images.forEach(image -> image.setProduct(product));
    variants.forEach(variant -> variant.setProduct(product));
    inventories.forEach(inventory -> inventory.setProduct(product));

    persistProductVariants(productRepository.save(product));
    // return ProductMapper.toProductDetail(savedProduct);
  }

  @CacheEvict(value = "productSearch", allEntries = true)
  public void indexProducts(List<ESProductVariant> products) throws IOException {
    BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();
    for (ESProductVariant product : products) {
      bulkRequestBuilder.operations(op -> op
          .index(idx -> idx
              .index("products")
              .id(product.getId().toString())
              .document(product)));
    }

    elasticsearchClient.bulk(bulkRequestBuilder.build());
  }

  @Override
  public void persistProductVariants(EProduct product) {
    List<ESProductVariant> esProductVariants = ProductESMapper.toESProductVariants(product);
    productESRepository.persistAllProductVariants(esProductVariants);
  }

  @Override
  public void updateProduct(TAccountRequest accountRequest, UpdateProductRequest updateProductRequest) {

    EProduct product = productRepository.findById(updateProductRequest.id())
        .orElseThrow(() -> new EntityNotFoundException("Not found product"));
    validateUpdatePermission(accountRequest, product);

    if (updateProductRequest.name() != null) {
      product.setName(updateProductRequest.name());
    }

    if (updateProductRequest.description() != null) {
      product.setDescription(updateProductRequest.description());
    }

    if (updateProductRequest.slug() != null) {
      product.setSlug(updateProductRequest.slug());
    }

    // if (updateProductRequest.categoryCode() != null) {
    // ECategory category =
    // categoryRepository.findByCode(updateProductRequest.categoryCode())
    // .orElseThrow(() -> new EntityNotFoundException("Not found category"));
    // product.setCategory(category);
    // } else if (updateProductRequest.categoryCode() == null) {
    // }
    // if (updateProductRequest.brandCode() != null) {
    // EBrand brand = brandRepository.findByCode(updateProductRequest.brandCode())
    // .orElseThrow(() -> new EntityNotFoundException("Not found brand"));
    // product.setBrand(brand);
    // }

    if (updateProductRequest.features() != null) {
      product.setFeatures(updateProductRequest.features());
    }
    if (updateProductRequest.isPublished() != null) {
      product.setPublished(updateProductRequest.isPublished());
    }

    productRepository.save(product);

    persistProductVariants(product);
  }

  private void validateUpdatePermission(TAccountRequest accountRequest, EProduct product) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'validateUpdatePermission'");
  }

  @Override
  public void deleteProduct(TAccountRequest accountRequest, UUID productId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteProduct'");
  }
}
