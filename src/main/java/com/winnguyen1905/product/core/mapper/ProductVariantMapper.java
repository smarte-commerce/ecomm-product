package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.winnguyen1905.product.core.model.ProductVariant;
import com.winnguyen1905.product.persistance.entity.EProductVariant;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductVariantMapper {
  EProductVariant toVariantEntity(ProductVariant productVariant);
}
