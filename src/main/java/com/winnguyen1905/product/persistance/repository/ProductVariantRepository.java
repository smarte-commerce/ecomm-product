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

  // Add method aliases for sync service compatibility
  @Query("select pv from EProductVariant pv where pv.product.id = :productId")
  List<EProductVariant> findByProductId(UUID productId);

  // Add method for inventory sync - find variants by inventory's product
  @Query("select pv from EProductVariant pv where pv.product.id in (select i.product.id from EInventory i where i.id = :inventoryId)")
  List<EProductVariant> findByInventoryId(UUID inventoryId);

  List<EProductVariant> findAllByIdIn(Set<UUID> ids);
}
