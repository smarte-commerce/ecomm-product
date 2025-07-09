package com.winnguyen1905.product.config;

import java.util.List;
import java.util.UUID;

/**
 * Fixed UUIDs for seed data
 * Product 1: 00000000-0000-0000-0000-000000000001 - Nike Air Max 270
 *   - Variant 1: 10000000-0000-0000-0000-000000000001 - Black/White
 *   - Variant 2: 10000000-0000-0000-0000-000000000002 - White/Black
 * 
 * Product 2: 00000000-0000-0000-0000-000000000002 - Adidas Ultraboost 21
 *   - Variant 1: 20000000-0000-0000-0000-000000000001 - Core Black
 *   - Variant 2: 20000000-0000-0000-0000-000000000002 - White
 * 
 * Product 3: 00000000-0000-0000-0000-000000000003 - Nike Dri-FIT T-Shirt
 *   - Variant 1: 30000000-0000-0000-0000-000000000001 - Black - M
 *   - Variant 2: 30000000-0000-0000-0000-000000000002 - Black - L
 *   - Variant 3: 30000000-0000-0000-0000-000000000003 - White - M
 */

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;
import com.winnguyen1905.product.secure.RegionPartition;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DatabaseInitializer implements CommandLineRunner {

  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final InventoryRepository inventoryRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private record VariantData(UUID id, String name, double price, String sku, String color, String size) {
  }

  private VariantData createVariantData(UUID id, String name, double price, String sku, String color, String size) {
    return new VariantData(id, name, price, sku, color, size);
  }

  // ► RFC‑4122‑compliant (version‑4, variant 1) UUIDs
  // ──────────────────────────────────────────────────

  // Product IDs (product‑no. encoded in the first 8 hex digits)
  public static final UUID PRODUCT_1_ID = UUID.fromString("00000001-0000-4000-8000-000000000001");
  public static final UUID PRODUCT_2_ID = UUID.fromString("00000002-0000-4000-8000-000000000002");
  public static final UUID PRODUCT_3_ID = UUID.fromString("00000003-0000-4000-8000-000000000003");

  // Variant IDs (product‑no. + variant‑no.)
  public static final UUID VARIANT_1_1 = UUID.fromString("00000001-0001-4000-8000-000000000011");
  public static final UUID VARIANT_1_2 = UUID.fromString("00000001-0002-4000-8000-000000000012");
  public static final UUID VARIANT_2_1 = UUID.fromString("00000002-0001-4000-8000-000000000021");
  public static final UUID VARIANT_2_2 = UUID.fromString("00000002-0002-4000-8000-000000000022");
  public static final UUID VARIANT_3_1 = UUID.fromString("00000003-0001-4000-8000-000000000031");
  public static final UUID VARIANT_3_2 = UUID.fromString("00000003-0002-4000-8000-000000000032");
  public static final UUID VARIANT_3_3 = UUID.fromString("00000003-0003-4000-8000-000000000033");

  // Shop ID
  public static final UUID SHOP_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

  @Override
  public void run(String... args) {
    try {
      log.info("Starting database initialization...");

      // Create regional products for testing gateway routing
      
      // US Region Products
      createProductWithVariants(
          PRODUCT_1_ID,
          "Nike Air Max 270",
          "Nike Air Max 270 - Comfortable running shoes (US Market)",
          RegionPartition.US,
          List.of(
              createVariantData(VARIANT_1_1, "Black/White", 150.0, "NIKE-AM270-BW-US", "Black/White", "9"),
              createVariantData(VARIANT_1_2, "White/Black", 150.0, "NIKE-AM270-WB-US", "White/Black", "9")));

      // EU Region Products
      createProductWithVariants(
          PRODUCT_2_ID,
          "Adidas Ultraboost 21",
          "Adidas Ultraboost 21 - Premium running shoes (EU Market)",
          RegionPartition.EU,
          List.of(
              createVariantData(VARIANT_2_1, "Core Black", 180.0, "ADIDAS-UB21-CB-EU", "Black", "42"),
              createVariantData(VARIANT_2_2, "White", 180.0, "ADIDAS-UB21-WH-EU", "White", "42")));

      // ASIA Region Products
      createProductWithVariants(
          PRODUCT_3_ID,
          "Nike Dri-FIT T-Shirt",
          "Nike Dri-FIT T-Shirt - Lightweight and breathable (ASIA Market)",
          RegionPartition.ASIA,
          List.of(
              createVariantData(VARIANT_3_1, "Black - M", 29.99, "NIKE-TS-BL-M-ASIA", "Black", "M"),
              createVariantData(VARIANT_3_2, "Black - L", 29.99, "NIKE-TS-BL-L-ASIA", "Black", "L"),
              createVariantData(VARIANT_3_3, "White - M", 29.99, "NIKE-TS-WH-M-ASIA", "White", "M")));

      log.info("Database initialization completed successfully!");
    } catch (Exception e) {
      log.error("Error during database initialization", e);
    }
  }

  private void createProductWithVariants(UUID productId, String name, String description,
      RegionPartition region, List<VariantData> variantsData) {
    try {
      // Check if product already exists by ID to avoid duplicates
      if (productRepository.existsById(productId)) {
        log.info("Product '{}' already exists, skipping...", name);
        return;
      }

      // Create product with specified region
      EProduct product = EProduct.builder()
          .id(productId)
          .name(name)
          .description(description)
          .isPublished(true)
          .shopId(SHOP_ID)
          .productType(ProductType.FASHION)
          .region(region)
          .build();
      
      EProductVariant variant = createProductVariants(product, variantsData);
      EInventory inventory = createInventory(product, variant.getSku(), region);
      product.getInventories().add(inventory);

      EProduct savedProduct = productRepository.save(product);
      log.info("Created product '{}' with {} variants for region {}", name, variantsData.size(), region.getCode());

    } catch (Exception e) {
      log.error("Error creating product: {} for region {}", name, region.getCode(), e);
    }
  }

  private EProductVariant createProductVariants(EProduct product, List<VariantData> variantsData) {
    for (VariantData variantData : variantsData) {
      try {
        // Create features JSON
        ObjectNode features = objectMapper.createObjectNode()
            .put("color", variantData.color())
            .put("size", variantData.size());

        // Create and save product variant with fixed ID
        EProductVariant variant = EProductVariant.builder()
            .id(variantData.id())
            .sku(variantData.sku())
            .price(variantData.price())
            .features(features)
            .isDeleted(false)
            .product(product)
            .build();
        return variant;

      } catch (Exception e) {
        log.error("Error creating variant: {}", variantData.sku(), e);
      }
    }
    return null;
  }

  private EInventory createInventory(EProduct product, String sku, RegionPartition region) {
    try {
      // Regional warehouse addresses
      String warehouseAddress = switch (region) {
        case US -> "Warehouse A, Los Angeles, CA, USA";
        case EU -> "Warehouse B, Amsterdam, Netherlands";
        case ASIA -> "Warehouse C, District 1, Ho Chi Minh City, Vietnam";
      };

      EInventory inventory = EInventory.builder()
          .sku(sku)
          .quantityReserved(0)
          .quantitySold(0)
          .address(warehouseAddress)
          .isDeleted(false)
          .quantityAvailable(100)
          .product(product)
          .build();
      
      return inventory;

    } catch (Exception e) {
      log.error("Error creating inventory: {} for region {}", sku, region.getCode(), e);
    }
    return null;
  }
}
