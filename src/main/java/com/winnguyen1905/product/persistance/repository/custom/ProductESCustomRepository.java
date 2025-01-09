package com.winnguyen1905.product.persistance.repository.custom;

import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;

public interface ProductESCustomRepository {
  List<ESProductVariant> findByIds(List<String> ids);
  Iterable<ESProductVariant> persistAllProductVariants(List<ESProductVariant> esProductVariants);
  SearchHits<ESProductVariant> searchProducts(SearchProductRequest searchProductRequest, Class<ESProductVariant> clazz);
}
