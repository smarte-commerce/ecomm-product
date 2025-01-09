package com.winnguyen1905.product.persistance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EVariation;
import com.winnguyen1905.product.persistance.repository.custom.SoftDeleteRepository;

@Repository
public interface VariationRepository extends JpaRepository<EVariation, UUID>,
    SoftDeleteRepository<EVariation, UUID> {
}
