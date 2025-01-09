package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.winnguyen1905.product.core.model.ProductDetail;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EProduct;

import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
  @Mapping(source = "brand", target = "brand", qualifiedByName = "stringToBrand")
  @Mapping(source = "category", target = "category.name")
  EProduct toProductEntity(ProductDetail product);

  @Named("stringToBrand")
  default EBrand stringToBrand(String brandName) {
    if (brandName == null) return null;
    return EBrand.builder().name(brandName).build();
  }

  @Mapping(source = "brand.name", target = "brand")
  @Mapping(target = "category", source = "category.name")
  ProductDetail toProductDto(EProduct product);

  default String map(ECategory value) {
    return value != null ? value.getName() : null;
  }
}
