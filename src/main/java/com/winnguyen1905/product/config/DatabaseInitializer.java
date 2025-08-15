package com.winnguyen1905.product.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.winnguyen1905.product.common.constant.ProductImageType;
import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.core.elasticsearch.service.ProductSyncService;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.BrandRepository;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.persistance.repository.InventoryRepository;
import com.winnguyen1905.product.persistance.repository.ProductImageRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.ProductVariantRepository;
import com.winnguyen1905.product.secure.RegionPartition;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Comprehensive Database Initializer with Elasticsearch Sync
 * 
 * This initializer creates:
 * - Multiple brands across all regions
 * - Hierarchical category structure  
 * - Diverse products with variants and images
 * - Complete inventory data
 * - Regional distribution for testing
 * - Automatic Elasticsearch synchronization
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Profile("!local")  // Don't run in local development
public class DatabaseInitializer implements CommandLineRunner {

  // Repositories
  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final InventoryRepository inventoryRepository;
  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;
  private final ProductImageRepository productImageRepository;
  
  // Optional Elasticsearch sync service - will be null if not available
  @Autowired(required = false)
  private ProductSyncService productSyncService;
  
  private final ObjectMapper objectMapper = new ObjectMapper();

  // Fixed UUIDs for consistent testing
  
  // Vendor/Shop IDs
  public static final UUID SHOP_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
  public static final UUID VENDOR_US_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
  public static final UUID VENDOR_EU_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
  public static final UUID VENDOR_ASIA_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");

  // Brand IDs
  public static final UUID BRAND_NIKE_ID = UUID.fromString("01000000-0000-4000-8000-000000000001");
  public static final UUID BRAND_ADIDAS_ID = UUID.fromString("01000000-0000-4000-8000-000000000002");
  public static final UUID BRAND_APPLE_ID = UUID.fromString("01000000-0000-4000-8000-000000000003");
  public static final UUID BRAND_SAMSUNG_ID = UUID.fromString("01000000-0000-4000-8000-000000000004");
  public static final UUID BRAND_IKEA_ID = UUID.fromString("01000000-0000-4000-8000-000000000005");
  public static final UUID BRAND_SONY_ID = UUID.fromString("01000000-0000-4000-8000-000000000006");

  // Category IDs
  public static final UUID CAT_ELECTRONICS_ID = UUID.fromString("02000000-0000-4000-8000-000000000001");
  public static final UUID CAT_SMARTPHONES_ID = UUID.fromString("02000000-0000-4000-8000-000000000002");
  public static final UUID CAT_LAPTOPS_ID = UUID.fromString("02000000-0000-4000-8000-000000000003");
  public static final UUID CAT_FASHION_ID = UUID.fromString("02000000-0000-4000-8000-000000000004");
  public static final UUID CAT_SHOES_ID = UUID.fromString("02000000-0000-4000-8000-000000000005");
  public static final UUID CAT_CLOTHING_ID = UUID.fromString("02000000-0000-4000-8000-000000000006");
  public static final UUID CAT_FURNITURE_ID = UUID.fromString("02000000-0000-4000-8000-000000000007");
  public static final UUID CAT_HOME_OFFICE_ID = UUID.fromString("02000000-0000-4000-8000-000000000008");

  // Product IDs
  public static final UUID PRODUCT_IPHONE_15_ID = UUID.fromString("03000000-0000-4000-8000-000000000001");
  public static final UUID PRODUCT_SAMSUNG_S24_ID = UUID.fromString("03000000-0000-4000-8000-000000000002");
  public static final UUID PRODUCT_NIKE_AIR_MAX_ID = UUID.fromString("03000000-0000-4000-8000-000000000003");
  public static final UUID PRODUCT_ADIDAS_ULTRABOOST_ID = UUID.fromString("03000000-0000-4000-8000-000000000004");
  public static final UUID PRODUCT_IKEA_DESK_ID = UUID.fromString("03000000-0000-4000-8000-000000000005");
  public static final UUID PRODUCT_SONY_HEADPHONES_ID = UUID.fromString("03000000-0000-4000-8000-000000000006");

  @Override
  public void run(String... args) {
    try {
      log.info("Starting comprehensive database initialization...");

      // Check if data already exists to avoid duplicates
      if (productRepository.count() > 0) {
        log.info("Database already contains data, skipping initialization.");
        return;
      }

      // 1. Create Brands
      createBrands();
      log.info("✓ Brands created");

      // 2. Create Categories
      createCategories();
      log.info("✓ Categories created");

      // 3. Create Products with Variants, Images, and Inventory
      createProducts();
      log.info("✓ Products created");

      // 4. Sync to Elasticsearch
      syncToElasticsearch();
      log.info("✓ Elasticsearch sync completed");

      log.info("Database initialization completed successfully!");
      logInitializationSummary();

    } catch (Exception e) {
      log.error("Error during database initialization", e);
      throw new RuntimeException("Database initialization failed", e);
    }
  }

  private void createBrands() {
    List<EBrand> brands = Arrays.asList(
        createBrand(BRAND_NIKE_ID, "Nike", "NIKE", "Just Do It", RegionPartition.US, VENDOR_US_ID, 
                   "https://images.example.com/brands/nike-logo.png", "https://www.nike.com", true),
        createBrand(BRAND_ADIDAS_ID, "Adidas", "ADIDAS", "Impossible is Nothing", RegionPartition.EU, VENDOR_EU_ID,
                   "https://images.example.com/brands/adidas-logo.png", "https://www.adidas.com", true),
        createBrand(BRAND_APPLE_ID, "Apple", "APPLE", "Think Different", RegionPartition.US, VENDOR_US_ID,
                   "https://images.example.com/brands/apple-logo.png", "https://www.apple.com", true),
        createBrand(BRAND_SAMSUNG_ID, "Samsung", "SAMSUNG", "Do What You Can't", RegionPartition.ASIA, VENDOR_ASIA_ID,
                   "https://images.example.com/brands/samsung-logo.png", "https://www.samsung.com", true),
        createBrand(BRAND_IKEA_ID, "IKEA", "IKEA", "The Life Improvement Store", RegionPartition.EU, VENDOR_EU_ID,
                   "https://images.example.com/brands/ikea-logo.png", "https://www.ikea.com", true),
        createBrand(BRAND_SONY_ID, "Sony", "SONY", "Be Moved", RegionPartition.ASIA, VENDOR_ASIA_ID,
                   "https://images.example.com/brands/sony-logo.png", "https://www.sony.com", true)
    );

    brandRepository.saveAll(brands);
  }

  private EBrand createBrand(UUID id, String name, String code, String description, RegionPartition region, 
                            UUID vendorId, String logoUrl, String websiteUrl, boolean isVerified) {
    return EBrand.builder()
        .id(id)
        .name(name)
        .code(code)
        .description(description)
        .logoUrl(logoUrl)
        .websiteUrl(websiteUrl)
        .isVerified(isVerified)
        .isActive(true)
        .region(region)
        .vendorId(vendorId)
        .isGlobalBrand(true)
        .productCount(0)
        .totalSales(0.0)
        .createdBy("system")
        .build();
  }

  private void createCategories() {
    List<ECategory> categories = new ArrayList<>();

    // Root categories
    ECategory electronics = createRootCategory(CAT_ELECTRONICS_ID, "Electronics", "ELECTRONICS", 
                                              "Electronic devices and gadgets", RegionPartition.US, VENDOR_US_ID);
    ECategory fashion = createRootCategory(CAT_FASHION_ID, "Fashion", "FASHION", 
                                         "Clothing, shoes and accessories", RegionPartition.EU, VENDOR_EU_ID);
    ECategory furniture = createRootCategory(CAT_FURNITURE_ID, "Furniture", "FURNITURE", 
                                           "Home and office furniture", RegionPartition.ASIA, VENDOR_ASIA_ID);

    categories.addAll(Arrays.asList(electronics, fashion, furniture));
    categoryRepository.saveAll(categories);

    // Child categories
    List<ECategory> childCategories = new ArrayList<>();
    
    // Electronics subcategories
    childCategories.add(createChildCategory(CAT_SMARTPHONES_ID, "Smartphones", "SMARTPHONES", 
                                          "Mobile phones and accessories", electronics, RegionPartition.US, VENDOR_US_ID));
    childCategories.add(createChildCategory(CAT_LAPTOPS_ID, "Laptops", "LAPTOPS", 
                                          "Notebook computers", electronics, RegionPartition.US, VENDOR_US_ID));

    // Fashion subcategories
    childCategories.add(createChildCategory(CAT_SHOES_ID, "Shoes", "SHOES", 
                                          "Athletic and casual footwear", fashion, RegionPartition.EU, VENDOR_EU_ID));
    childCategories.add(createChildCategory(CAT_CLOTHING_ID, "Clothing", "CLOTHING", 
                                          "Shirts, pants, and outerwear", fashion, RegionPartition.EU, VENDOR_EU_ID));

    // Furniture subcategories
    childCategories.add(createChildCategory(CAT_HOME_OFFICE_ID, "Home Office", "HOME_OFFICE", 
                                          "Desks, chairs, and office furniture", furniture, RegionPartition.ASIA, VENDOR_ASIA_ID));

    categoryRepository.saveAll(childCategories);
  }

  private ECategory createRootCategory(UUID id, String name, String code, String description, 
                                      RegionPartition region, UUID vendorId) {
    return ECategory.builder()
        .id(id)
        .name(name)
        .code(code)
        .description(description)
        .isPublished(true)
        .isFeatured(true)
        .displayOrder(0)
        .region(region)
        .vendorId(vendorId)
        .isGlobalCategory(true)
        .categoryLevel(0)
        .categoryPath(name)
        .productCount(0)
        .totalSales(0.0)
        .createdBy("system")
        .build();
  }

  private ECategory createChildCategory(UUID id, String name, String code, String description, 
                                       ECategory parent, RegionPartition region, UUID vendorId) {
    return ECategory.builder()
        .id(id)
        .name(name)
        .code(code)
        .description(description)
        .parent(parent)
        .isPublished(true)
        .isFeatured(false)
        .displayOrder(0)
        .region(region)
        .vendorId(vendorId)
        .isGlobalCategory(false)
        .categoryLevel(1)
        .categoryPath(parent.getName() + "/" + name)
        .productCount(0)
        .totalSales(0.0)
        .createdBy("system")
        .build();
  }

  private void createProducts() {
    // iPhone 15 (US) - Electronics/Smartphones
    createIPhone15();
    
    // Samsung Galaxy S24 (ASIA) - Electronics/Smartphones
    createSamsungGalaxyS24();
    
    // Nike Air Max (US) - Fashion/Shoes
    createNikeAirMax();
    
    // Adidas Ultraboost (EU) - Fashion/Shoes
    createAdidasUltraboost();
    
    // IKEA Desk (EU) - Furniture/Home Office
    createIkeaDesk();
    
    // Sony Headphones (ASIA) - Electronics
    createSonyHeadphones();
  }

  private void createIPhone15() {
    EProduct product = createBaseProduct(
        PRODUCT_IPHONE_15_ID, "iPhone 15", "Apple iPhone 15 with advanced camera system",
        ProductType.ELECTRONIC, ProductStatus.ACTIVE, RegionPartition.US, VENDOR_US_ID, SHOP_ID,
        getBrandById(BRAND_APPLE_ID), getCategoryById(CAT_SMARTPHONES_ID),
        BigDecimal.valueOf(799.00), "iphone,smartphone,apple,mobile"
    );

    List<EProductVariant> variants = Arrays.asList(
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000001"), 
                           "iPhone 15 - Pink 128GB", 799.00, "IPHONE15-PINK-128GB", 
                           createPhoneFeatures("Pink", "128GB"), product, RegionPartition.US, VENDOR_US_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000002"), 
                           "iPhone 15 - Blue 256GB", 899.00, "IPHONE15-BLUE-256GB", 
                           createPhoneFeatures("Blue", "256GB"), product, RegionPartition.US, VENDOR_US_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000003"), 
                           "iPhone 15 - Black 512GB", 1099.00, "IPHONE15-BLACK-512GB", 
                           createPhoneFeatures("Black", "512GB"), product, RegionPartition.US, VENDOR_US_ID)
    );

    saveProductWithVariantsAndImages(product, variants, createIPhoneImages());
  }

  private void createSamsungGalaxyS24() {
    EProduct product = createBaseProduct(
        PRODUCT_SAMSUNG_S24_ID, "Galaxy S24", "Samsung Galaxy S24 with AI features",
        ProductType.ELECTRONIC, ProductStatus.ACTIVE, RegionPartition.ASIA, VENDOR_ASIA_ID, SHOP_ID,
        getBrandById(BRAND_SAMSUNG_ID), getCategoryById(CAT_SMARTPHONES_ID),
        BigDecimal.valueOf(699.00), "samsung,galaxy,smartphone,android"
    );

    List<EProductVariant> variants = Arrays.asList(
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000004"), 
                           "Galaxy S24 - Titanium Gray 256GB", 699.00, "GALAXY-S24-GRAY-256GB", 
                           createPhoneFeatures("Titanium Gray", "256GB"), product, RegionPartition.ASIA, VENDOR_ASIA_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000005"), 
                           "Galaxy S24 - Violet 512GB", 799.00, "GALAXY-S24-VIOLET-512GB", 
                           createPhoneFeatures("Violet", "512GB"), product, RegionPartition.ASIA, VENDOR_ASIA_ID)
    );

    saveProductWithVariantsAndImages(product, variants, createSamsungImages());
  }

  private void createNikeAirMax() {
    EProduct product = createBaseProduct(
        PRODUCT_NIKE_AIR_MAX_ID, "Nike Air Max 270", "Nike Air Max 270 running shoes with Air cushioning",
        ProductType.FASHION, ProductStatus.ACTIVE, RegionPartition.US, VENDOR_US_ID, SHOP_ID,
        getBrandById(BRAND_NIKE_ID), getCategoryById(CAT_SHOES_ID),
        BigDecimal.valueOf(150.00), "nike,shoes,running,athletic,airmax"
    );

    List<EProductVariant> variants = Arrays.asList(
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000006"), 
                           "Air Max 270 Black US 9", 150.00, "NIKE-AM270-BLACK-US9", 
                           createShoeFeatures("Black", "US 9"), product, RegionPartition.US, VENDOR_US_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000007"), 
                           "Air Max 270 White US 10", 150.00, "NIKE-AM270-WHITE-US10", 
                           createShoeFeatures("White", "US 10"), product, RegionPartition.US, VENDOR_US_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000008"), 
                           "Air Max 270 Red US 11", 150.00, "NIKE-AM270-RED-US11", 
                           createShoeFeatures("Red", "US 11"), product, RegionPartition.US, VENDOR_US_ID)
    );

    saveProductWithVariantsAndImages(product, variants, createNikeImages());
  }

  private void createAdidasUltraboost() {
    EProduct product = createBaseProduct(
        PRODUCT_ADIDAS_ULTRABOOST_ID, "Adidas Ultraboost 22", "Adidas Ultraboost 22 with Boost technology",
        ProductType.FASHION, ProductStatus.ACTIVE, RegionPartition.EU, VENDOR_EU_ID, SHOP_ID,
        getBrandById(BRAND_ADIDAS_ID), getCategoryById(CAT_SHOES_ID),
        BigDecimal.valueOf(180.00), "adidas,ultraboost,running,boost"
    );

    List<EProductVariant> variants = Arrays.asList(
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000009"), 
                           "Ultraboost 22 Core Black EU 42", 180.00, "ADIDAS-UB22-BLACK-EU42", 
                           createShoeFeatures("Core Black", "EU 42"), product, RegionPartition.EU, VENDOR_EU_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000010"), 
                           "Ultraboost 22 White EU 43", 180.00, "ADIDAS-UB22-WHITE-EU43", 
                           createShoeFeatures("White", "EU 43"), product, RegionPartition.EU, VENDOR_EU_ID)
    );

    saveProductWithVariantsAndImages(product, variants, createAdidasImages());
  }

  private void createIkeaDesk() {
    EProduct product = createBaseProduct(
        PRODUCT_IKEA_DESK_ID, "BEKANT Desk", "IKEA BEKANT desk with adjustable legs",
        ProductType.FURNITURE, ProductStatus.ACTIVE, RegionPartition.ASIA, VENDOR_ASIA_ID, SHOP_ID,
        getBrandById(BRAND_IKEA_ID), getCategoryById(CAT_HOME_OFFICE_ID),
        BigDecimal.valueOf(129.00), "ikea,desk,office,furniture,bekant"
    );

    List<EProductVariant> variants = Arrays.asList(
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000011"), 
                           "BEKANT Desk White 120x80cm", 129.00, "IKEA-BEKANT-WHITE-120X80", 
                           createFurnitureFeatures("White", "120x80cm"), product, RegionPartition.ASIA, VENDOR_ASIA_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000012"), 
                           "BEKANT Desk Black-brown 160x80cm", 149.00, "IKEA-BEKANT-BROWN-160X80", 
                           createFurnitureFeatures("Black-brown", "160x80cm"), product, RegionPartition.ASIA, VENDOR_ASIA_ID)
    );

    saveProductWithVariantsAndImages(product, variants, createIkeaImages());
  }

  private void createSonyHeadphones() {
    EProduct product = createBaseProduct(
        PRODUCT_SONY_HEADPHONES_ID, "Sony WH-1000XM5", "Sony WH-1000XM5 Wireless Noise Canceling Headphones",
        ProductType.ELECTRONIC, ProductStatus.ACTIVE, RegionPartition.ASIA, VENDOR_ASIA_ID, SHOP_ID,
        getBrandById(BRAND_SONY_ID), getCategoryById(CAT_ELECTRONICS_ID),
        BigDecimal.valueOf(349.00), "sony,headphones,wireless,noise-canceling"
    );

    List<EProductVariant> variants = Arrays.asList(
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000013"), 
                           "WH-1000XM5 Black", 349.00, "SONY-WH1000XM5-BLACK", 
                           createHeadphoneFeatures("Black", "Over-ear"), product, RegionPartition.ASIA, VENDOR_ASIA_ID),
        createProductVariant(UUID.fromString("04000000-0000-4000-8000-000000000014"), 
                           "WH-1000XM5 Silver", 349.00, "SONY-WH1000XM5-SILVER", 
                           createHeadphoneFeatures("Silver", "Over-ear"), product, RegionPartition.ASIA, VENDOR_ASIA_ID)
    );

    saveProductWithVariantsAndImages(product, variants, createSonyImages());
  }

  // Helper methods for creating products and variants

  private EProduct createBaseProduct(UUID id, String name, String description, ProductType type, 
                                   ProductStatus status, RegionPartition region, UUID vendorId, UUID shopId,
                                   EBrand brand, ECategory category, BigDecimal basePrice, String tags) {
    return EProduct.builder()
        .id(id)
        .name(name)
        .description(description)
        .shortDescription(description.length() > 100 ? description.substring(0, 100) + "..." : description)
        .productType(type)
        .status(status)
        .isPublished(true)
        .region(region)
        .vendorId(vendorId)
        .shopId(shopId)
        .brand(brand)
        .category(category)
        .basePrice(basePrice)
        .trackInventory(true)
        .allowBackorder(false)
        .lowStockThreshold(10)
        .requiresShipping(true)
        .viewCount(0L)
        .purchaseCount(0L)
        .ratingCount(0)
        .tags(tags)
        .metaTitle(name)
        .metaDescription(description)
        .metaKeywords(tags.replace(",", ", "))
        .createdBy("system")
        .build();
  }

  private EProductVariant createProductVariant(UUID id, String name, Double price, String sku, 
                                             ObjectNode features, EProduct product, RegionPartition region, UUID vendorId) {
    return EProductVariant.builder()
        .id(id)
        .name(name)
        .sku(sku)
        .price(price)
        .compareAtPrice(BigDecimal.valueOf(price * 1.2)) // 20% markup for comparison
        .costPrice(BigDecimal.valueOf(price * 0.6)) // 60% of price as cost
        .features(features)
        .isActive(true)
        .isDefault(false)
        .trackInventory(true)
        .inventoryQuantity(100)
        .reservedQuantity(0)
        .viewCount(0L)
        .purchaseCount(0L)
        .product(product)
        .region(region)
        .vendorId(vendorId)
        .createdBy("system")
        .build();
  }

  private void saveProductWithVariantsAndImages(EProduct product, List<EProductVariant> variants, List<EProductImage> images) {
    // Save product first
    EProduct savedProduct = productRepository.save(product);
    
    // Save variants
    variants.forEach(variant -> variant.setProduct(savedProduct));
    List<EProductVariant> savedVariants = productVariantRepository.saveAll(variants);
    
    // Create inventory for each variant
    List<EInventory> inventories = new ArrayList<>();
    for (EProductVariant variant : savedVariants) {
      inventories.add(createInventory(variant));
    }
    inventoryRepository.saveAll(inventories);
    
    // Save images
    images.forEach(image -> image.setProduct(savedProduct));
    productImageRepository.saveAll(images);
    
    log.info("Created product: {} with {} variants and {} images", product.getName(), variants.size(), images.size());
  }

  private EInventory createInventory(EProductVariant variant) {
    String warehouseAddress = getWarehouseAddress(variant.getRegion());
    
    return EInventory.builder()
        .id(UUID.randomUUID())
        .sku(variant.getSku())
        .product(variant.getProduct())
        .quantityAvailable(variant.getInventoryQuantity())
        .quantityReserved(variant.getReservedQuantity())
        .quantitySold(0)
        .address(warehouseAddress)
        .isDeleted(false)
        .build();
  }

  private String getWarehouseAddress(RegionPartition region) {
    return switch (region) {
      case US -> "Warehouse A, Los Angeles, CA, USA";
      case EU -> "Warehouse B, Amsterdam, Netherlands";
      case ASIA -> "Warehouse C, District 1, Ho Chi Minh City, Vietnam";
    };
  }

  // Feature creation methods
  private ObjectNode createPhoneFeatures(String color, String storage) {
    ObjectNode features = objectMapper.createObjectNode();
    features.put("color", color);
    features.put("storage", storage);
    features.put("display_size", "6.1 inches");
    features.put("camera", "48MP main camera");
    features.put("battery", "All-day battery life");
    features.put("connectivity", "5G");
    return features;
  }

  private ObjectNode createShoeFeatures(String color, String size) {
    ObjectNode features = objectMapper.createObjectNode();
    features.put("color", color);
    features.put("size", size);
    features.put("material", "Mesh and synthetic");
    features.put("sole", "Air cushioning");
    features.put("gender", "Unisex");
    return features;
  }

  private ObjectNode createFurnitureFeatures(String color, String dimensions) {
    ObjectNode features = objectMapper.createObjectNode();
    features.put("color", color);
    features.put("dimensions", dimensions);
    features.put("material", "Particleboard with melamine surface");
    features.put("adjustable_legs", "Yes");
    features.put("weight_capacity", "50 kg");
    return features;
  }

  private ObjectNode createHeadphoneFeatures(String color, String type) {
    ObjectNode features = objectMapper.createObjectNode();
    features.put("color", color);
    features.put("type", type);
    features.put("noise_canceling", "Yes");
    features.put("battery_life", "30 hours");
    features.put("connectivity", "Bluetooth 5.2");
    features.put("microphone", "Built-in");
    return features;
  }

  // Image creation methods
  private List<EProductImage> createIPhoneImages() {
    return Arrays.asList(
        createProductImage("https://images.example.com/iphone15/pink-front.jpg", "iPhone 15 Pink Front View", ProductImageType.PRODUCT_PREVIEW_IMAGE, true, 1),
        createProductImage("https://images.example.com/iphone15/pink-back.jpg", "iPhone 15 Pink Back View", ProductImageType.PRODUCT_PREVIEW_IMAGE, false, 2),
        createProductImage("https://images.example.com/iphone15/blue-front.jpg", "iPhone 15 Blue Front View", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 3),
        createProductImage("https://images.example.com/iphone15/black-front.jpg", "iPhone 15 Black Front View", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 4)
    );
  }

  private List<EProductImage> createSamsungImages() {
    return Arrays.asList(
        createProductImage("https://images.example.com/galaxy-s24/gray-front.jpg", "Galaxy S24 Gray Front", ProductImageType.PRODUCT_PREVIEW_IMAGE, true, 1),
        createProductImage("https://images.example.com/galaxy-s24/gray-back.jpg", "Galaxy S24 Gray Back", ProductImageType.PRODUCT_PREVIEW_IMAGE, false, 2),
        createProductImage("https://images.example.com/galaxy-s24/violet-front.jpg", "Galaxy S24 Violet Front", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 3)
    );
  }

  private List<EProductImage> createNikeImages() {
    return Arrays.asList(
        createProductImage("https://images.example.com/nike/airmax-black-side.jpg", "Nike Air Max Black Side View", ProductImageType.PRODUCT_PREVIEW_IMAGE, true, 1),
        createProductImage("https://images.example.com/nike/airmax-white-side.jpg", "Nike Air Max White Side View", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 2),
        createProductImage("https://images.example.com/nike/airmax-red-side.jpg", "Nike Air Max Red Side View", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 3)
    );
  }

  private List<EProductImage> createAdidasImages() {
    return Arrays.asList(
        createProductImage("https://images.example.com/adidas/ultraboost-black.jpg", "Adidas Ultraboost Black", ProductImageType.PRODUCT_PREVIEW_IMAGE, true, 1),
        createProductImage("https://images.example.com/adidas/ultraboost-white.jpg", "Adidas Ultraboost White", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 2)
    );
  }

  private List<EProductImage> createIkeaImages() {
    return Arrays.asList(
        createProductImage("https://images.example.com/ikea/bekant-white.jpg", "IKEA BEKANT White Desk", ProductImageType.PRODUCT_PREVIEW_IMAGE, true, 1),
        createProductImage("https://images.example.com/ikea/bekant-brown.jpg", "IKEA BEKANT Brown Desk", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 2)
    );
  }

  private List<EProductImage> createSonyImages() {
    return Arrays.asList(
        createProductImage("https://images.example.com/sony/wh1000xm5-black.jpg", "Sony WH-1000XM5 Black", ProductImageType.PRODUCT_PREVIEW_IMAGE, true, 1),
        createProductImage("https://images.example.com/sony/wh1000xm5-silver.jpg", "Sony WH-1000XM5 Silver", ProductImageType.PRODUCT_VARIANT_IMAGE, false, 2)
    );
  }

  private EProductImage createProductImage(String url, String altText, ProductImageType type, boolean isPrimary, int displayOrder) {
    return EProductImage.builder()
        .url(url)
        .altText(altText)
        .title(altText)
        .type(type)
        .isPrimary(isPrimary)
        .isActive(true)
        .displayOrder(displayOrder)
        .fileName(extractFileNameFromUrl(url))
        .mimeType("image/jpeg")
        .width(800)
        .height(600)
        .storageProvider("S3")
        .vendorId(VENDOR_US_ID) // Default vendor
        .createdBy("system")
        .build();
  }

  private String extractFileNameFromUrl(String url) {
    return url.substring(url.lastIndexOf('/') + 1);
  }

  // Helper methods to get entities by ID
  private EBrand getBrandById(UUID id) {
    return brandRepository.findById(id).orElse(null);
  }

  private ECategory getCategoryById(UUID id) {
    return categoryRepository.findById(id).orElse(null);
  }

  private void syncToElasticsearch() {
    try {
      log.info("Starting Elasticsearch synchronization...");
      
      if (productSyncService != null) {
        // Trigger full reindex asynchronously
        productSyncService.fullReindex();
        
        // Wait a moment for async operation to start
        Thread.sleep(2000);
        
        log.info("Elasticsearch synchronization initiated successfully");
      } else {
        log.info("Elasticsearch sync service not available - skipping synchronization");
      }
    } catch (Exception e) {
      log.error("Error during Elasticsearch synchronization: {}", e.getMessage(), e);
      // Don't fail the entire initialization if ES sync fails
    }
  }

  private void logInitializationSummary() {
    long brandCount = brandRepository.count();
    long categoryCount = categoryRepository.count();
    long productCount = productRepository.count();
    long variantCount = productVariantRepository.count();
    long inventoryCount = inventoryRepository.count();
    long imageCount = productImageRepository.count();

    log.info("\n" +
            "=== DATABASE INITIALIZATION SUMMARY ===\n" +
            "Brands created: {}\n" +
            "Categories created: {}\n" +
            "Products created: {}\n" +
            "Product variants created: {}\n" +
            "Inventory records created: {}\n" +
            "Product images created: {}\n" +
            "========================================",
            brandCount, categoryCount, productCount, variantCount, inventoryCount, imageCount);
  }
}
