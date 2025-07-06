package com.winnguyen1905.product.core.elasticsearch.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;
import com.winnguyen1905.product.secure.RegionPartition;

/**
 * Elasticsearch repository for product variant operations
 */
@Repository
public interface ESProductVariantRepository extends ElasticsearchRepository<ESProductVariant, UUID> {

    /**
     * Find variants by product ID
     */
    List<ESProductVariant> findByProductId(UUID productId);

    /**
     * Find variants by region
     */
    Page<ESProductVariant> findByRegion(RegionPartition region, Pageable pageable);

    /**
     * Find variants by price range
     */
    @Query("{\"range\": {\"price\": {\"gte\": ?0, \"lte\": ?1}}}")
    Page<ESProductVariant> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * Find variants by brand
     */
    Page<ESProductVariant> findByBrand(String brand, Pageable pageable);

    /**
     * Search variants by name
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"description^2\", \"brand^2\"]}}")
    Page<ESProductVariant> findByNameContaining(String searchTerm, Pageable pageable);

    /**
     * Find variants with available inventory
     */
    @Query("{\"range\": {\"inventory.quantityAvailable\": {\"gt\": 0}}}")
    Page<ESProductVariant> findVariantsInStock(Pageable pageable);

    /**
     * Find variants with low stock
     */
    @Query("{\"range\": {\"inventory.quantityAvailable\": {\"lt\": ?0, \"gt\": 0}}}")
    Page<ESProductVariant> findVariantsLowStock(Integer threshold, Pageable pageable);

    /**
     * Find variants out of stock
     */
    @Query("{\"bool\": {\"should\": [{\"bool\": {\"must_not\": {\"exists\": {\"field\": \"inventory.quantityAvailable\"}}}}, {\"term\": {\"inventory.quantityAvailable\": 0}}]}}")
    Page<ESProductVariant> findVariantsOutOfStock(Pageable pageable);

    /**
     * Find popular variants by product IDs
     */
    @Query("{\"bool\": {\"must\": [{\"terms\": {\"productId\": [?0]}}]}}")
    List<ESProductVariant> findByProductIdIn(List<UUID> productIds);

    /**
     * Advanced search with filters
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"description^2\", \"brand^2\"]}}], \"filter\": [{\"range\": {\"price\": {\"gte\": ?1, \"lte\": ?2}}}, {\"range\": {\"inventory.quantityAvailable\": {\"gt\": 0}}}]}}")
    Page<ESProductVariant> searchWithFilters(String searchTerm, Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * Find variants by specific features (JsonNode search)
     */
    @Query("{\"bool\": {\"must\": [{\"nested\": {\"path\": \"features\", \"query\": {\"bool\": {\"must\": [{\"match\": {\"features.?0\": \"?1\"}}]}}}}]}}")
    Page<ESProductVariant> findByFeature(String featureName, String featureValue, Pageable pageable);
} 
