package com.winnguyen1905.product.core.elasticsearch.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnguyen1905.product.core.elasticsearch.document.ProductDocument;
import com.winnguyen1905.product.persistance.elasticsearch.ESInventory;
import com.winnguyen1905.product.persistance.elasticsearch.ESProductVariant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!local")  // Exclude from local profile
@RequiredArgsConstructor
public class ElasticsearchIndexService {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ObjectMapper objectMapper;
    private volatile boolean indexInitialized = false;

    @EventListener
    @Order(1) // Run early in the context lifecycle
    public void handleContextRefresh(ContextRefreshedEvent event) {
        if (!indexInitialized) {
            initializeAllIndices();
            indexInitialized = true;
        }
    }

    public void initializeAllIndices() {
        log.info("Initializing all Elasticsearch indices...");
        try {
            createProductIndex();
            createInventoryIndex();
            createProductVariantIndex();
            log.info("All Elasticsearch indices initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch indices", e);
        }
    }

    public void createProductIndex() {
        try {
            IndexOperations indexOps = elasticsearchTemplate.indexOps(ProductDocument.class);
            
            // Check if index exists without creating it
            if (indexOps.exists()) {
                log.info("Products index already exists");
                return;
            }

            // Read settings and mappings from JSON file
            String settingsJson = loadJsonFromClasspath("elasticsearch/product-settings.json");
            JsonNode settingsNode = objectMapper.readTree(settingsJson);
            
            // Extract settings and mappings
            JsonNode settings = settingsNode.get("settings");
            JsonNode mappings = settingsNode.get("mappings");
            
            // Create index with settings
            if (settings != null) {
                Document settingsDoc = Document.parse(settings.toString());
                indexOps.create(settingsDoc);
                log.info("Created products index with custom settings");
            } else {
                indexOps.create();
                log.info("Created products index with default settings");
            }
            
            // Apply mappings
            if (mappings != null) {
                Document mappingDoc = Document.parse(mappings.toString());
                indexOps.putMapping(mappingDoc);
                log.info("Applied custom mappings to products index");
            }
            
            log.info("Successfully initialized Elasticsearch products index");
            
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch products index: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
        }
    }

    public void createInventoryIndex() {
        createBasicIndex(ESInventory.class, "inventory");
    }

    public void createProductVariantIndex() {
        createBasicIndex(ESProductVariant.class, "product_variants");
    }

    private void createBasicIndex(Class<?> documentClass, String indexName) {
        try {
            IndexOperations indexOps = elasticsearchTemplate.indexOps(documentClass);
            
            if (indexOps.exists()) {
                log.info("Index '{}' already exists", indexName);
                return;
            }

            indexOps.create();
            indexOps.putMapping();
            
            log.info("Successfully created Elasticsearch index '{}'", indexName);
            
        } catch (Exception e) {
            log.error("Failed to create Elasticsearch index '{}': {}", indexName, e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
        }
    }

    public void recreateProductIndex() {
        log.info("Recreating products index...");
        createProductIndex();
    }

    private String loadJsonFromClasspath(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    public boolean indexExists() {
        try {
            IndexOperations indexOps = elasticsearchTemplate.indexOps(ProductDocument.class);
            return indexOps.exists();
        } catch (Exception e) {
            log.error("Error checking if index exists: {}", e.getMessage());
            return false;
        }
    }

    public boolean ensureIndexExists(String indexName, Class<?> documentClass) {
        try {
            IndexOperations indexOps = elasticsearchTemplate.indexOps(documentClass);
            if (!indexOps.exists()) {
                log.warn("Index '{}' does not exist, creating it now", indexName);
                createBasicIndex(documentClass, indexName);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to ensure index '{}' exists", indexName, e);
            return false;
        }
    }

    public void deleteIndex() {
        try {
            IndexOperations indexOps = elasticsearchTemplate.indexOps(ProductDocument.class);
            if (indexOps.exists()) {
                indexOps.delete();
                log.info("Deleted products index");
            }
        } catch (Exception e) {
            log.error("Error deleting index: {}", e.getMessage());
            throw new RuntimeException("Failed to delete index", e);
        }
    }
}
