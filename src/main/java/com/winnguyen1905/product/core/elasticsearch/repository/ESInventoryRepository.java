package com.winnguyen1905.product.core.elasticsearch.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.elasticsearch.ESInventory;

/**
 * Elasticsearch repository for inventory operations
 */
@Repository
public interface ESInventoryRepository extends ElasticsearchRepository<ESInventory, UUID> {

    /**
     * Find inventory by SKU
     */
    Optional<ESInventory> findBySku(String sku);

    /**
     * Find inventories with available quantity greater than specified amount
     */
    @Query("{\"range\": {\"quantityAvailable\": {\"gt\": ?0}}}")
    List<ESInventory> findByQuantityAvailableGreaterThan(Integer quantity);

    /**
     * Find inventories with low stock (available quantity less than threshold)
     */
    @Query("{\"range\": {\"quantityAvailable\": {\"lt\": ?0}}}")
    Page<ESInventory> findLowStockInventories(Integer threshold, Pageable pageable);

    /**
     * Find inventories that are out of stock
     */
    @Query("{\"bool\": {\"should\": [{\"bool\": {\"must_not\": {\"exists\": {\"field\": \"quantityAvailable\"}}}}, {\"term\": {\"quantityAvailable\": 0}}]}}")
    List<ESInventory> findOutOfStockInventories();

    /**
     * Find inventories with reserved quantities
     */
    @Query("{\"range\": {\"quantityReserved\": {\"gt\": 0}}}")
    List<ESInventory> findInventoriesWithReservations();

    /**
     * Search inventories by SKU pattern
     */
    @Query("{\"wildcard\": {\"sku\": \"*?0*\"}}")
    Page<ESInventory> findBySkuContaining(String skuPattern, Pageable pageable);
} 
