package com.winnguyen1905.product.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EInventory;

@Repository
public interface InventoryRepository extends JpaRepository<EInventory, UUID> {
	Optional<EInventory> findBySku(String sku);
}
