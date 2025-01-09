package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.winnguyen1905.product.core.model.Variant;
import com.winnguyen1905.product.persistance.entity.EVariation;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VariantMapper {
  EVariation toVariantEntity(Variant variant);
}
