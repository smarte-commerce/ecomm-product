package com.winnguyen1905.product.core.service.vendor;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.core.mapper_v2.InventoryMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductESMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductImageMapper;
import com.winnguyen1905.product.core.mapper_v2.ProductMapper;
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

  private final BrandRepository brandRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductESCustomRepository productESRepository;

  @Override
  @Transactional
  public Mono<ProductDetail> addProduct(UUID shopId, AddProductRequest request) {
    List<EProductImage> images = CommonUtils.stream(request.images())
      .map(ProductImageMapper::toProductImageEntity)
      .toList();
    List<EProductVariant> variants = CommonUtils.stream(request.variations())
      .map(ProductMapper::toProductVariantEntity)
      .toList();
    List<EInventory> inventories = CommonUtils.stream(request.inventories())
      .map(InventoryMapper::toInventoryEntity)
      .toList();

    Mono<EBrand> brand = Mono.fromCallable(() -> brandRepository.findByCode(request.brandCode())
      .orElseThrow(() -> new EntityNotFoundException("Not found brand")))
      .subscribeOn(Schedulers.boundedElastic());

    Mono<ECategory> category = Mono
      .fromCallable(() -> categoryRepository.findByCodeAndShopId(request.categoryCode(), shopId)
        .orElseThrow(() -> new EntityNotFoundException("Not found category")))
      .subscribeOn(Schedulers.boundedElastic());

    return Mono.zip(brand, category)
      .flatMap(tuple -> {
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

        return Mono.fromCallable(() -> productRepository.save(product))
          .flatMap(savedProduct -> this.persistProductVariants(savedProduct)
            .thenReturn(ProductMapper.toProductDetail(savedProduct)));
      })
      .subscribeOn(Schedulers.boundedElastic());
            
  }

  @Override
  public Mono<Void> persistProductVariants(EProduct product) {
    return Mono.just(ProductESMapper.toESProductVariants(product))
        .publishOn(Schedulers.boundedElastic())
        .map(esProductVariant -> this.productESRepository.persistAllProductVariants(esProductVariant))
        .flatMap(__ -> Mono.empty());
  }
}
