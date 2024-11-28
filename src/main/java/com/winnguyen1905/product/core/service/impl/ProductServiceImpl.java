package com.winnguyen1905.product.core.service.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.core.converter.ProductConverter;
import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.request.UpdateProductRequest;
import com.winnguyen1905.product.core.model.response.PagedResponse;
import com.winnguyen1905.product.core.service.ProductService;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EVariation;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.util.NormalSpecificationUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ModelMapper mapper;
  private final ProductConverter productConverter;
  private final ProductRepository productRepository;
  private final Type pagedResponseType = new TypeToken<PagedResponse<Product>>() {}.getType();

  @Override
  public Product handleAddProduct(UUID shopId, AddProductRequest addProductRequest) {
    EProduct product = this.productConverter.toProductEntity(addProductRequest);
    product.setShopId(shopId);

    for (EVariation variation : product.getVariations()) {
      variation.setInventories(variation.getInventories());
      product.getVariations().add(variation);
    }

    product = this.productRepository.save(product);
    return this.productConverter.toProduct(product);
  }

  public Product handleUpdateProduct(UpdateProductRequest updateProductRequest) {
    return null;
  }

  @Override
  public PagedResponse<Product> handleGetAllProducts(SearchProductRequest productSearchRequest, Pageable pageable) {
    List<Specification<EProduct>> specList = NormalSpecificationUtils.toNormalSpec(productSearchRequest);
    Page<EProduct> productPages = this.productRepository.findAll(Specification.allOf(specList), pageable);
    return this.mapper.map(productPages, pagedResponseType);
  }

  @Override
  public List<Product> handleChangeProductStatus(UUID shopId, List<UUID> ids) {
    List<EProduct> products = this.productRepository.findByIdInAndShopId(ids, shopId);
    if (products.size() != ids.size()) {
      throw new ResourceNotFoundException(
          "Cannot update because " + products.size() + " of " + ids.size() + " product be found");
    }
    products = products.stream().map(item -> {
      item.setIsDraft(!item.getIsDraft());
      item.setIsPublished(!item.getIsPublished());
      return item;
    }).toList();

    products = this.productRepository.saveAll(products);
    return products.stream().map(item -> (Product) this.productConverter.toProduct(item)).toList();
  }

  @Override
  public Product handleGetProduct(UUID id) {
    EProduct product = this.productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Not found product id " + id.toString()));
    if (!product.getIsPublished()) {
      throw new ResourceNotFoundException("Not found product id " + id.toString());
    }
    return this.productConverter.toProduct(product);
  }

  @Override
  public void handleDeleteProducts(UUID shopId, List<UUID> ids) {
    List<EProduct> products = this.productRepository.findByIdInAndShopId(ids, shopId);
    this.productRepository.softDeleteMany(products);
  }
}
