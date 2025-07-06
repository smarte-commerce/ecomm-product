package com.winnguyen1905.product.core.elasticsearch.service;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.core.elasticsearch.mapper.ProductDocumentMapper;
import com.winnguyen1905.product.core.elasticsearch.repository.ProductElasticsearchRepository;
import com.winnguyen1905.product.exception.BusinessLogicException;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSyncService {

    private final ProductElasticsearchRepository productElasticsearchRepository;
    private final ProductDocumentMapper productDocumentMapper;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Async
    @CacheEvict(value = {"product-search", "product-category-search", "product-price-search", "similar-products", "popular-products", "product-suggestions"}, allEntries = true)
    public void syncProduct(UUID productId) {
        log.info("Syncing product with ID: {}", productId);
        
        try {
            EProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            
            List<EProductVariant> variants = productVariantRepository.findByProductId(productId);
            
            for (EProductVariant variant : variants) {
                ProductDocument document = productDocumentMapper.fromProductEntity(product, variant);
                if (document != null) {
                    productElasticsearchRepository.save(document);
                    log.debug("Synced product variant: {} for product: {}", variant.getId(), productId);
                }
            }
            
            log.info("Successfully synced product: {} with {} variants", productId, variants.size());
        } catch (Exception e) {
            log.error("Error syncing product {}: {}", productId, e.getMessage(), e);
            throw new BusinessLogicException("Failed to sync product: " + e.getMessage());
        }
    }

    @Async
    @CacheEvict(value = {"product-search", "product-category-search", "product-price-search", "similar-products", "popular-products", "product-suggestions"}, allEntries = true)
    public void syncProducts(List<UUID> productIds) {
        log.info("Syncing {} products", productIds.size());
        
        try {
            List<EProduct> products = productRepository.findAllById(productIds);
            
            for (EProduct product : products) {
                List<EProductVariant> variants = productVariantRepository.findByProductId(product.getId());
                
                for (EProductVariant variant : variants) {
                    ProductDocument document = productDocumentMapper.fromProductEntity(product, variant);
                    if (document != null) {
                        productElasticsearchRepository.save(document);
                    }
                }
            }
            
            log.info("Successfully synced {} products", products.size());
        } catch (Exception e) {
            log.error("Error syncing products: {}", e.getMessage(), e);
            throw new BusinessLogicException("Failed to sync products: " + e.getMessage());
        }
    }

    @Async
    @CacheEvict(value = {"product-search", "product-category-search", "product-price-search", "similar-products", "popular-products", "product-suggestions"}, allEntries = true)
    public void syncInventory(UUID inventoryId) {
        log.info("Syncing inventory with ID: {}", inventoryId);
        
        try {
            List<EProductVariant> variants = productVariantRepository.findByInventoryId(inventoryId);
            
            for (EProductVariant variant : variants) {
                EProduct product = variant.getProduct();
                if (product != null) {
                    ProductDocument document = productDocumentMapper.fromProductEntity(product, variant);
                    if (document != null) {
                        productElasticsearchRepository.save(document);
                        log.debug("Synced inventory for variant: {}", variant.getId());
                    }
                }
            }
            
            log.info("Successfully synced inventory: {} for {} variants", inventoryId, variants.size());
        } catch (Exception e) {
            log.error("Error syncing inventory {}: {}", inventoryId, e.getMessage(), e);
            throw new BusinessLogicException("Failed to sync inventory: " + e.getMessage());
        }
    }

    @Async
    @CacheEvict(value = {"product-search", "product-category-search", "product-price-search", "similar-products", "popular-products", "product-suggestions"}, allEntries = true)
    public void deleteProduct(UUID productId) {
        log.info("Deleting product from Elasticsearch with ID: {}", productId);
        
        try {
            List<EProductVariant> variants = productVariantRepository.findByProductId(productId);
            
            for (EProductVariant variant : variants) {
                productElasticsearchRepository.deleteById(variant.getId().toString());
                log.debug("Deleted product variant: {} for product: {}", variant.getId(), productId);
            }
            
            log.info("Successfully deleted product: {} with {} variants from Elasticsearch", productId, variants.size());
        } catch (Exception e) {
            log.error("Error deleting product {}: {}", productId, e.getMessage(), e);
            throw new BusinessLogicException("Failed to delete product: " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = {"product-search", "product-category-search", "product-price-search", "similar-products", "popular-products", "product-suggestions"}, allEntries = true)
    public void fullReindex() {
        log.info("Starting full reindex of products");
        
        try {
            // Clear existing index
            productElasticsearchRepository.deleteAll();
            log.info("Cleared existing Elasticsearch index");
            
            // Get all published products
            List<EProduct> products = productRepository.findAll();
            int totalProducts = products.size();
            int processedProducts = 0;
            
            for (EProduct product : products) {
                if (Boolean.TRUE.equals(product.getIsPublished())) {
                    List<EProductVariant> variants = productVariantRepository.findByProductId(product.getId());
                    
                    for (EProductVariant variant : variants) {
                        ProductDocument document = productDocumentMapper.fromProductEntity(product, variant);
                        if (document != null) {
                            productElasticsearchRepository.save(document);
                        }
                    }
                    
                    processedProducts++;
                    if (processedProducts % 100 == 0) {
                        log.info("Reindexed {}/{} products", processedProducts, totalProducts);
                    }
                }
            }
            
            log.info("Successfully completed full reindex of {} products", processedProducts);
        } catch (Exception e) {
            log.error("Error during full reindex: {}", e.getMessage(), e);
            throw new BusinessLogicException("Failed to perform full reindex: " + e.getMessage());
        }
    }

    public long getDocumentCount() {
        return productElasticsearchRepository.count();
    }

    public boolean isHealthy() {
        try {
            long count = getDocumentCount();
            log.debug("Elasticsearch health check: {} documents in index", count);
            return true;
        } catch (Exception e) {
            log.error("Elasticsearch health check failed: {}", e.getMessage());
            return false;
        }
    }
} 
