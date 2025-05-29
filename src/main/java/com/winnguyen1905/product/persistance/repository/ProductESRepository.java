  package com.winnguyen1905.product.persistance.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.repository.custom.ProductESCustomRepository;

@Primary
@Repository
public interface ProductESRepository extends ProductESCustomRepository {
}
