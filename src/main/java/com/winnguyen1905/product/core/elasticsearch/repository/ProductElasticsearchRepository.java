package com.winnguyen1905.product.core.elasticsearch.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.secure.RegionPartition;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

  /**
   * Find products by category ID
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"category.id\": \"?0\"}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findByCategoryId(String categoryId, Pageable pageable);

  /**
   * Find products by brand ID
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"brand.id\": \"?0\"}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findByBrandId(String brandId, Pageable pageable);

  /**
   * Find products by shop ID
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"shop_id\": \"?0\"}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findByShopId(UUID shopId, Pageable pageable);

  /**
   * Find products by region
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"region\": \"?0\"}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findByRegion(RegionPartition region, Pageable pageable);

  /**
   * Find products by price range
   */
  @Query("{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": ?0, \"lte\": ?1}}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

  /**
   * Find featured products
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"is_featured\": true}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findFeaturedProducts(Pageable pageable);

  /**
   * Find products by SKU
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"sku\": \"?0\"}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  List<ProductDocument> findBySku(String sku);

  /**
   * Find products by tag
   */
  @Query("{\"bool\": {\"must\": [{\"terms\": {\"tags\": [\"?0\"]}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findByTag(String tag, Pageable pageable);

  /**
   * Find products with high rating
   */
  @Query("{\"bool\": {\"must\": [{\"range\": {\"rating\": {\"gte\": ?0}}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findByRatingGreaterThanEqual(Double rating, Pageable pageable);

  /**
   * Find products in stock
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"inventory.is_in_stock\": true}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findInStockProducts(Pageable pageable);

  /**
   * Find low stock products
   */
  @Query("{\"bool\": {\"must\": [{\"term\": {\"inventory.is_low_stock\": true}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findLowStockProducts(Pageable pageable);

  /**
   * Find popular products (ordered by purchase count)
   */
  @Query("{\"bool\": {\"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findPopularProducts(Pageable pageable);

  /**
   * Find similar products (placeholder - would need more complex implementation)
   */
  @Query("{\"bool\": {\"must_not\": [{\"term\": {\"id\": \"?0\"}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findSimilarProducts(String productId, Pageable pageable);

  /**
   * Text search suggestions
   */
  @Query("{\"bool\": {\"must\": [{\"prefix\": {\"name\": \"?0\"}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  List<String> findSuggestions(String query);

  /**
   * Advanced search with multiple criteria
   */
  @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"description^2\", \"brand.name^2\"]}}], \"filter\": [{\"term\": {\"is_published\": true}}]}}")
  Page<ProductDocument> findBySearchTerm(String searchTerm, Pageable pageable);
}
