package com.winnguyen1905.product.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EDiscount;

@Repository
public interface DiscountRepository extends JpaRepository<EDiscount, UUID>, JpaSpecificationExecutor<EDiscount> {
  void deleteByIdIn(List<UUID> ids);

  EDiscount findByCodeAndShopId(String code, UUID shopId); // REAL

  Optional<EDiscount> findByIdAndIsActiveTrue(UUID id);

  Page<EDiscount> findAllByShopIdAndIsActiveTrue(UUID shopId, Pageable pageable);
}
