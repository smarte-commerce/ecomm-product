package com.winnguyen1905.product.core.service.vendor;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.core.mapper.InventoryMapper;
import com.winnguyen1905.product.core.mapper.ProductESMapper;
import com.winnguyen1905.product.core.mapper.ProductImageMapper;
import com.winnguyen1905.product.core.mapper.ProductMapper;
import com.winnguyen1905.product.core.mapper.ProductVariantMapper;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.response.ProductDetail;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.BrandRepository;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.persistance.repository.ProductESRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;
import com.winnguyen1905.product.util.CommonUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorProductServiceImpl implements VendorProductService {

  private final ProductMapper productMapper;
  private final InventoryMapper inventoryMapper;
  private final ProductImageMapper productImageMapper;
  private final BrandRepository brandRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductESMapper productESMapper;
  private final ProductESCustomRepository productESRepository;
  private final ProductVariantMapper productVariantMapper;

  @Override
  @Transactional
  public Mono<ProductDetail> addProduct(UUID shopId, AddProductRequest request) {

    List<EProductImage> images = CommonUtils.stream(request.images())
        .map(productImageMapper::toProductImageEntity)
        .toList();
    List<EProductVariant> variants = CommonUtils.stream(request.variations())
        .map(productVariantMapper::toVariantEntity)
        .toList();
    List<EInventory> inventories = CommonUtils.stream(request.inventories())
        .map(inventoryMapper::toInventoryEntity)
        .toList();

    Mono<EBrand> brand = Mono.fromCallable(() -> brandRepository.findByCode(request.brandCode())
        .orElseThrow(() -> new EntityNotFoundException("Not found brand"))).subscribeOn(Schedulers.boundedElastic());

    Mono<ECategory> category = Mono
        .fromCallable(() -> categoryRepository.findByCodeAndShopId(request.categoryCode(), shopId)
            .orElseThrow(() -> new EntityNotFoundException("Not found category")))
        .subscribeOn(Schedulers.boundedElastic());

    return Mono.zip(brand, category)
        .map((Tuple2<EBrand, ECategory> tuple) -> {
          final EProduct product = EProduct.builder()
              .name(request.name())
              .description(request.description())
              .features(CommonUtils.fromObject(request.features()))
              .isDraft(true)
              .isPublished(false)
              .brand(tuple.getT1())
              .shopId(shopId)
              .category(tuple.getT2())
              .images(images)
              .variations(variants)
              .inventories(inventories)
              .build();

          images.forEach(image -> image.setProduct(product));
          variants.forEach(variant -> variant.setProduct(product));
          inventories.forEach(inventory -> inventory.setProduct(product));

          return product;
        })
        .flatMap(product -> Mono.fromCallable(() -> productRepository.save(product)))
        .map(product -> {
          this.persistProductVariants(product);
          return productMapper.toProductDto(product);
        }).subscribeOn(Schedulers.boundedElastic());
            
  }

  @Override
  public Mono<Void> persistProductVariants(EProduct product) {
    return Mono.just(this.productESMapper.toESProductVariants(product))
        .publishOn(Schedulers.boundedElastic())
        .map(esProductVariant -> this.productESRepository.persistAllProductVariants(esProductVariant))
        .flatMap(__ -> Mono.empty());
  }
}
