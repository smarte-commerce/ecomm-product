package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.winnguyen1905.product.core.model.response.CategoryResponse;
import com.winnguyen1905.product.persistance.elasticsearch.ESCategory;
import com.winnguyen1905.product.persistance.entity.ECategory;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
  CategoryResponse toCategory(ECategory category);
  CategoryResponse toCategory(ESCategory category);
  ESCategory toESCategory(ECategory category);
}
