package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.winnguyen1905.product.core.model.ProductImage;
import com.winnguyen1905.product.persistance.entity.EProductImage;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductImageMapper {
  EProductImage toProductImageEntity(ProductImage productImage);
}
