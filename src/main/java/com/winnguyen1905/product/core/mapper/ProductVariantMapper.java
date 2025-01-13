package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.winnguyen1905.product.core.model.ProductVariantDetail;
import com.winnguyen1905.product.core.model.response.ProductVariantReview;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.entity.EProductVariant;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductVariantMapper {
  EProductVariant toVariantEntity(ProductVariantDetail variant);
  ProductVariantReview toProductVariantResponse(ESProductVariant esProductVariant);
}
