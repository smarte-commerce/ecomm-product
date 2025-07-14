package com.winnguyen1905.product.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EInventory;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface InventoryRepository extends JpaRepository<EInventory, UUID> {
    Optional<EInventory> findBySku(String sku);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM EInventory i WHERE i.sku = :sku")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")})
    Optional<EInventory> findBySkuWithLock(@Param("sku") String sku);
    
    @Query("SELECT i FROM EInventory i WHERE i.product.id = :productId")
    Page<EInventory> findByProductId(@Param("productId") UUID productId, Pageable pageable);
    
    List<EInventory> findByProductId(UUID productId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM EInventory i WHERE i.id = :id")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")})
    Optional<EInventory> findByIdWithLock(@Param("id") UUID id);
    
    @Cacheable(value = "inventory", key = "#id")
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM EInventory i WHERE i.id = :id")
    Optional<EInventory> findByIdWithOptimisticLock(@Param("id") UUID id);
    
    @Cacheable(value = "inventory", key = "#sku")
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM EInventory i WHERE i.sku = :sku")
    Optional<EInventory> findBySkuWithOptimisticLock(@Param("sku") String sku);
}
