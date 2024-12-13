package com.winnguyen1905.product.core.builder;

import org.modelmapper.ModelMapper;

import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.persistance.entity.EProduct;

public class ProductBuilder {

  private static final ModelMapper modelMapper = new ModelMapper();

  public static final Product with(EProduct product) {
    Product productDto = modelMapper.map(product, Product.class);
    return productDto;
  }

  public static final EProduct with(AddProductRequest addProductRequest) {
    EProduct product = modelMapper.map(addProductRequest, EProduct.class);
    product.setDraft(true);
    product.setPublished(false);
    return product;
  }

}
