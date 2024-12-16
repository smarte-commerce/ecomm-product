package com.winnguyen1905.product.persistance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.custom.SoftDeleteRepository;

@Repository
public interface VariationRepository extends JpaRepository<EProductVariant, UUID>, SoftDeleteRepository<EProductVariant, UUID> {
}
