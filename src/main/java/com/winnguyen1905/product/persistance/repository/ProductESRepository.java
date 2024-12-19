package com.winnguyen1905.product.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;

@Repository
public interface ProductESRepository
    extends ProductESCustomRepository {
}
