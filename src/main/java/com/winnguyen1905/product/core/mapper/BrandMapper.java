package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.winnguyen1905.product.core.model.Brand;
import com.winnguyen1905.product.persistance.entity.EBrand;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BrandMapper {
  EBrand toBrandEntity(Brand brand);
  Brand toBrand(EBrand ebrand);
}
