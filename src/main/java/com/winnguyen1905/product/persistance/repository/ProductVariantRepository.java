package com.winnguyen1905.product.persistance.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<EProductVariant, UUID> {
  @Query("select pv from EProductVariant pv where pv.product.id = :productId")
  List<EProductVariant> findVariantsByProductId(UUID productId);

  List<EProductVariant> findAllByIdIn(Set<UUID> ids);
}
