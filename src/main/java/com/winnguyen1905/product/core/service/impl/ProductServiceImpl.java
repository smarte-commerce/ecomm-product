package com.winnguyen1905.product.core.service.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.common.ProductImageType;
import com.winnguyen1905.product.core.builder.ProductBuilder;
import com.winnguyen1905.product.core.mapper.ProductMapper;
import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.response.Category;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.core.service.ProductService;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EVariation;
import com.winnguyen1905.product.persistance.repository.BrandRepository;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.VariationRepository;
import com.winnguyen1905.product.util.CommonUtils;
import com.winnguyen1905.product.util.OptionalExtractor;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ModelMapper mapper;
  private final BrandRepository brandRepository;
  private final ProductMapper productConverter;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final Type pagedResponseType = new TypeToken<PagedResponse<Product>>() {
  }.getType();

  @Override
  @Transactional
  public Product addProduct(UUID shopId, AddProductRequest addProductRequest) {
    List<EProductImage> productImages = CommonUtils.stream(addProductRequest.images())
        .map(item -> this.mapper.map(item, EProductImage.class))
        .toList();

    List<EVariation> productVariations = CommonUtils.stream(addProductRequest.variations())
        .map(item -> this.mapper.map(item, EVariation.class))
        .toList();

    List<EInventory> productInventories = CommonUtils.stream(addProductRequest.inventories())
        .map(item -> this.mapper.map(item, EInventory.class))
        .toList();

    EBrand brand = this.brandRepository.findById(addProductRequest.brand().id())
        .orElseGet(() -> {
          EBrand newBrand = new EBrand();
          newBrand.setName(addProductRequest.brand().name());
          return this.brandRepository.save(newBrand);
        });

    ECategory category = this.categoryRepository.findByIdAndShopId(addProductRequest.category().id(), shopId)
        .orElse(handleGetCategoryEntity(addProductRequest.category(), shopId));

    EProduct product = ProductBuilder.with(addProductRequest);
    product.setBrand(brand);
    product.setShopId(shopId);
    product.setCategory(category);
    product.setImages(productImages);
    product.setVariations(productVariations);
    product.setInventories(productInventories);

    final EProduct finalProduct = product;
    brand.getProducts().add(finalProduct);
    category.getProducts().add(finalProduct);
    productImages.forEach(item -> item.setProduct(finalProduct));
    productVariations.forEach(item -> item.setProduct(finalProduct));
    productInventories.forEach(item -> item.setProduct(finalProduct));

    product = this.productRepository.save(product);
    return ProductBuilder.with(product);
  }

  private ECategory handleGetCategoryEntity(Category categoryDto, UUID shopId) {
    Optional<ECategory> optionalCategory = this.categoryRepository.findByIdAndShopId(categoryDto.id(), shopId);

    if (optionalCategory.isPresent()) return optionalCategory.get();

    ECategory newCategory = this.mapper.map(categoryDto, ECategory.class);
    newCategory.setShopId(shopId);

    if (newCategory.getParentId() == null) {
      Long numberOfCategory = this.categoryRepository.countByShopId(shopId);
      newCategory.setLeft(numberOfCategory * 2 + 1);
      newCategory.setRight(numberOfCategory * 2 + 2);
    } else {
      ECategory parentCategory = OptionalExtractor.fromOptional(
          this.categoryRepository.findByIdAndShopId(newCategory.getParentId(), shopId),
          "Not found parent category id " + newCategory.getParentId());
      newCategory.setLeft(parentCategory.getRight());
      newCategory.setRight(parentCategory.getRight() + 1);
      parentCategory.setRight(newCategory.getRight() + 1);
      this.categoryRepository.updateCategoryTreeOfShop(parentCategory.getRight(), shopId);
      this.categoryRepository.save(parentCategory);
    }
  
    return this.categoryRepository.save(newCategory);
  }

  @Override
  public Product getProduct(UUID id) {
    EProduct product = OptionalExtractor.fromOptional(this.productRepository.findById(id),
        "Not found product id " + id);
    if (!product.isPublished() || !product.getShopId().equals(OptionalExtractor.currentUserId()))
      throw new ResourceNotFoundException("Not found product " + id);
    return this.productConverter.map(product);
  }

  // @Override
  // public PagedResponse<Product> handleGetAllProducts(SearchProductRequest
  // productSearchRequest, Pageable pageable) {
  // List<Specification<EProduct>> specList =
  // NormalSpecificationUtils.toNormalSpec(productSearchRequest);
  // Page<EProduct> productPages =
  // this.productRepository.findAll(Specification.allOf(specList), pageable);
  // return this.mapper.map(productPages, pagedResponseType);
  // }

  // @Override
  // public List<Product> handleChangeProductStatus(UUID shopId, List<UUID> ids) {
  // List<EProduct> products = this.productRepository.findByIdInAndShopId(ids,
  // shopId);
  // if (products.size() != ids.size()) {
  // throw new ResourceNotFoundException(
  // "Cannot update because " + products.size() + " of " + ids.size() + " product
  // be found");
  // }
  // products = products.stream().map(item -> {
  // item.setIsDraft(!item.getIsDraft());
  // item.setIsPublished(!item.getIsPublished());
  // return item;
  // }).toList();

  // products = this.productRepository.saveAll(products);
  // return products.stream().map(item -> (Product)
  // this.productConverter.toProduct(item)).toList();
  // }

  // @Override
  // public void handleDeleteProducts(UUID shopId, List<UUID> ids) {
  // List<EProduct> products = this.productRepository.findByIdInAndShopId(ids,
  // shopId);
  // this.productRepository.softDeleteMany(products);
  // }
}
