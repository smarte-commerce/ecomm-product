package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.winnguyen1905.product.core.model.response.Category;
import com.winnguyen1905.product.persistance.elasticsearch.ESCategory;
import com.winnguyen1905.product.persistance.entity.ECategory;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
  ESCategory toESCategory(ECategory category);
  Category toCategory(ECategory category);
}
