package com.winnguyen1905.product.persistance.repository.garbage;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.garbage.EBrand;

@Repository
public interface BrandRepository extends JpaRepository<EBrand, UUID> {
  Optional<EBrand> findByName(String name);
  Optional<EBrand> findByCode(String code);
}
