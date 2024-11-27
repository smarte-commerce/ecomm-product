package com.winnguyen1905.product.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EUserDiscount;
import com.winnguyen1905.product.persistance.repository.custom.SoftDeleteRepository;

@Repository
public interface UserDiscountRepository extends JpaRepository<EUserDiscount, UUID>, SoftDeleteRepository<EUserDiscount, UUID> {
  Optional<EUserDiscount> findByUserIdAndDiscountId(UUID userId, UUID discountId);
}
