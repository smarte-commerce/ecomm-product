# Product Service API - Testing Scenarios Guide

## Overview

This guide provides comprehensive testing scenarios for the Product Service API using the seed data created by the DatabaseInitializer. All UUIDs referenced are from the actual seed data, ensuring reliable and repeatable testing.

## 🗂️ Seed Data Reference

### Fixed UUIDs for Testing

#### Vendors & Shop
- **Shop ID**: `11111111-1111-4111-8111-111111111111`
- **US Vendor**: `10000000-0000-4000-8000-000000000001`
- **EU Vendor**: `20000000-0000-4000-8000-000000000001`  
- **ASIA Vendor**: `30000000-0000-4000-8000-000000000001`

#### Brands (6 Total)
- **Nike (US)**: `01000000-0000-4000-8000-000000000001`
- **Adidas (EU)**: `01000000-0000-4000-8000-000000000002`
- **Apple (US)**: `01000000-0000-4000-8000-000000000003`
- **Samsung (ASIA)**: `01000000-0000-4000-8000-000000000004`
- **IKEA (EU)**: `01000000-0000-4000-8000-000000000005`
- **Sony (ASIA)**: `01000000-0000-4000-8000-000000000006`

#### Categories (8 Total - Hierarchical)
- **Electronics**: `02000000-0000-4000-8000-000000000001`
  - **Smartphones**: `02000000-0000-4000-8000-000000000002`
  - **Laptops**: `02000000-0000-4000-8000-000000000003`
- **Fashion**: `02000000-0000-4000-8000-000000000004`
  - **Shoes**: `02000000-0000-4000-8000-000000000005`
  - **Clothing**: `02000000-0000-4000-8000-000000000006`
- **Furniture**: `02000000-0000-4000-8000-000000000007`
  - **Home Office**: `02000000-0000-4000-8000-000000000008`

#### Products (6 Total with 14 Variants)
- **iPhone 15 (US)**: `03000000-0000-4000-8000-000000000001`
- **Samsung Galaxy S24 (ASIA)**: `03000000-0000-4000-8000-000000000002`
- **Nike Air Max 270 (US)**: `03000000-0000-4000-8000-000000000003`
- **Adidas Ultraboost 22 (EU)**: `03000000-0000-4000-8000-000000000004`
- **IKEA BEKANT Desk (ASIA)**: `03000000-0000-4000-8000-000000000005`
- **Sony WH-1000XM5 (ASIA)**: `03000000-0000-4000-8000-000000000006`

---

## 🧪 Testing Scenarios by Controller

### 1. Product Controller - Core CRUD Operations

#### Scenario 1.1: Product Lifecycle Management
**Goal**: Test complete product lifecycle from creation to deletion

1. **Create New Product**
   ```http
   POST /api/v1/products
   Authorization: Bearer {{vendorToken}}
   
   {
     "name": "Test Laptop Pro",
     "description": "High-performance laptop for developers",
     "productType": "ELECTRONIC",
     "vendorId": "{{vendorUSId}}",
     "shopId": "{{shopId}}",
     "region": "US",
     "brandId": "{{brandAppleId}}",
     "categoryId": "{{catLaptopsId}}",
     "basePrice": 1999.99
   }
   ```

2. **Get Product Details**
   ```http
   GET /api/v1/products/{{productIPhone15Id}}
   ```

3. **Update Product**
   ```http
   PUT /api/v1/products/{{productIPhone15Id}}
   Authorization: Bearer {{vendorToken}}
   
   {
     "name": "iPhone 15 Pro Max",
     "basePrice": 899.99
   }
   ```

4. **Delete Product**
   ```http
   DELETE /api/v1/products/{{productIPhone15Id}}
   Authorization: Bearer {{vendorToken}}
   ```

#### Scenario 1.2: Customer Product Discovery
**Goal**: Test public product access and variant exploration

1. **Browse Public Product**
   ```http
   GET /api/v1/products/public/{{productSamsungS24Id}}
   ```

2. **Get Product Variants**
   ```http
   GET /api/v1/products/{{productIPhone15Id}}/variants
   ```

3. **Get Product Images**
   ```http
   GET /api/v1/products/{{productNikeAirMaxId}}/images
   ```

---

### 2. Search Controller - Elasticsearch & Advanced Search

#### Scenario 2.1: Partition-First Search Testing
**Goal**: Test regional search optimization and multi-region fallback

1. **US Region Search (Partition-First)**
   ```http
   POST /api/v1/search/elasticsearch
   
   {
     "keyword": "iPhone",
     "region": "US",
     "partitionFirstEnabled": true,
     "partitionFirstThreshold": 0.7,
     "pagination": {
       "pageNum": 0,
       "pageSize": 10
     }
   }
   ```

2. **EU Region Search**
   ```http
   POST /api/v1/search/elasticsearch
   
   {
     "keyword": "Adidas",
     "region": "EU",
     "partitionFirstEnabled": true,
     "pagination": {
       "pageNum": 0,
       "pageSize": 10
     }
   }
   ```

3. **ASIA Region Search**
   ```http
   POST /api/v1/search/elasticsearch
   
   {
     "keyword": "Samsung",
     "region": "ASIA",
     "partitionFirstEnabled": true,
     "pagination": {
       "pageNum": 0,
       "pageSize": 10
     }
   }
   ```

#### Scenario 2.2: Advanced Search with Filters
**Goal**: Test complex search queries with multiple filters

1. **Category + Price Range Filter**
   ```http
   POST /api/v1/search/elasticsearch
   
   {
     "keyword": "smartphone",
     "filters": [
       {
         "field": "category",
         "values": ["Smartphones"],
         "operator": "in"
       },
       {
         "field": "priceRange",
         "values": ["500-1000"],
         "operator": "range"
       }
     ],
     "sorts": [
       {
         "field": "price",
         "order": "asc"
       }
     ]
   }
   ```

2. **Brand + Region Filter**
   ```http
   POST /api/v1/search/elasticsearch
   
   {
     "keyword": "",
     "filters": [
       {
         "field": "brand",
         "values": ["Nike", "Adidas"],
         "operator": "in"
       },
       {
         "field": "region",
         "values": ["US", "EU"],
         "operator": "in"
       }
     ]
   }
   ```

#### Scenario 2.3: Search Suggestions & SEO
**Goal**: Test autocomplete and SEO-friendly URLs

1. **Get Search Suggestions**
   ```http
   GET /api/v1/search/suggestions?term=iph
   GET /api/v1/search/suggestions?term=sam
   GET /api/v1/search/suggestions?term=nik
   ```

2. **Search by SEO Slug**
   ```http
   GET /api/v1/search/slug/iphone-15?vendorId={{vendorUSId}}
   GET /api/v1/search/slug/galaxy-s24?vendorId={{vendorAsiaId}}
   ```

---

### 3. Brand Controller - Brand Management

#### Scenario 3.1: Brand Discovery & Management
**Goal**: Test brand browsing and vendor-specific operations

1. **Browse All Brands**
   ```http
   GET /api/v1/brands?page=0&size=20
   ```

2. **Get Specific Brand Details**
   ```http
   GET /api/v1/brands/{{brandAppleId}}
   GET /api/v1/brands/{{brandSamsungId}}
   GET /api/v1/brands/{{brandNikeId}}
   ```

3. **Search Brands by Name**
   ```http
   GET /api/v1/brands/search?name=Apple
   GET /api/v1/brands/search?name=Samsung
   ```

4. **Get Vendor's Brands**
   ```http
   GET /api/v1/brands/vendor/{{vendorUSId}}
   Authorization: Bearer {{vendorToken}}
   ```

---

### 4. Category Controller - Hierarchical Navigation

#### Scenario 4.1: Category Tree Navigation
**Goal**: Test hierarchical category structure and product discovery

1. **Get All Categories**
   ```http
   GET /api/v1/categories
   ```

2. **Get Category Tree Structure**
   ```http
   GET /api/v1/categories/tree
   ```

3. **Browse Category Products**
   ```http
   GET /api/v1/categories/{{catElectronicsId}}/products
   GET /api/v1/categories/{{catSmartphonesId}}/products
   GET /api/v1/categories/{{catShoesId}}/products
   ```

4. **Search Categories**
   ```http
   GET /api/v1/categories/search?name=Electronics
   ```

---

### 5. Inventory Controller - Stock Management

#### Scenario 5.1: Inventory Operations & Reservations
**Goal**: Test inventory tracking and reservation system

1. **Check Product Inventory**
   ```http
   GET /api/v1/inventories/product/{{productIPhone15Id}}
   Authorization: Bearer {{vendorToken}}
   ```

2. **Get Inventory by SKU**
   ```http
   GET /api/v1/inventories/sku/IPHONE15-PINK-128GB
   ```

3. **Reserve Inventory**
   ```http
   PATCH /api/v1/inventories/reserve
   
   {
     "quantity": 2,
     "reservationId": "{{testOrderId}}",
     "expirationMinutes": 30
   }
   ```

4. **Release Reserved Inventory**
   ```http
   PATCH /api/v1/inventories/release
   
   {
     "quantity": 2,
     "reservationId": "{{testOrderId}}"
   }
   ```

5. **Batch Availability Check**
   ```http
   POST /api/v1/inventories/check-availability
   
   {
     "items": [
       {
         "sku": "IPHONE15-PINK-128GB",
         "quantity": 5
       },
       {
         "sku": "GALAXY-S24-GRAY-256GB",
         "quantity": 3
       }
     ]
   }
   ```

---

### 6. Vendor Controller - Business Operations

#### Scenario 6.1: Vendor Registration & Profile
**Goal**: Test vendor onboarding and profile management

1. **Register New Vendor**
   ```http
   POST /api/v1/vendors/register
   
   {
     "businessName": "TechCorp Electronics",
     "businessType": "CORPORATION",
     "contactPersonName": "Jane Smith",
     "email": "jane@techcorp.com",
     "phoneNumber": "+1555123456",
     "businessAddress": {
       "streetAddress": "456 Tech Avenue",
       "city": "Silicon Valley",
       "stateProvince": "CA",
       "postalCode": "94102",
       "countryCode": "US"
     },
     "region": "US",
     "acceptedTermsAndConditions": true,
     "acceptedPrivacyPolicy": true
   }
   ```

2. **Get Vendor Dashboard**
   ```http
   GET /api/v1/vendors/analytics/dashboard
   Authorization: Bearer {{vendorToken}}
   ```

3. **Get Product Performance Analytics**
   ```http
   GET /api/v1/vendors/analytics/products/performance?days=30
   Authorization: Bearer {{vendorToken}}
   ```

#### Scenario 6.2: SEO & Marketing Operations
**Goal**: Test vendor marketing tools and SEO optimization

1. **Update Product SEO**
   ```http
   PATCH /api/v1/vendors/products/{{productIPhone15Id}}/seo
   Authorization: Bearer {{vendorToken}}
   
   {
     "metaTitle": "iPhone 15 - Best Smartphone 2024 | TechStore",
     "metaDescription": "Get the latest iPhone 15 with advanced camera, A17 chip, and all-day battery life. Free shipping available.",
     "metaKeywords": "iPhone 15, smartphone, Apple, mobile phone, camera, 5G, A17 chip"
   }
   ```

2. **Sync Product Inventory**
   ```http
   POST /api/v1/vendors/products/{{productIPhone15Id}}/sync-inventory
   Authorization: Bearer {{vendorToken}}
   ```

---

### 7. Admin Controller - System Management

#### Scenario 7.1: Administrative Oversight
**Goal**: Test administrative functions and system monitoring

1. **Monitor All Inventories**
   ```http
   GET /api/v1/admin/inventories?page=0&size=20
   Authorization: Bearer {{adminToken}}
   ```

2. **Review Pending Products**
   ```http
   GET /api/v1/admin/products/pending-approval
   Authorization: Bearer {{adminToken}}
   ```

3. **Approve Product**
   ```http
   PATCH /api/v1/admin/products/{{productIPhone15Id}}/approve
   Authorization: Bearer {{adminToken}}
   
   {
     "isPublished": true,
     "rejectionReason": null
   }
   ```

#### Scenario 7.2: Bulk Operations
**Goal**: Test administrative bulk operations

1. **Bulk Update Product Status**
   ```http
   PATCH /api/v1/admin/products/bulk/status
   Authorization: Bearer {{adminToken}}
   
   {
     "productIds": [
       "{{productIPhone15Id}}",
       "{{productSamsungS24Id}}",
       "{{productNikeAirMaxId}}"
     ],
     "status": "ACTIVE"
   }
   ```

2. **Cache Management**
   ```http
   GET /api/v1/admin/system/cache/status
   POST /api/v1/admin/system/cache/clear
   Authorization: Bearer {{adminToken}}
   
   {
     "cacheName": "products"
   }
   ```

---

### 8. Regional Controller - Multi-Region Testing

#### Scenario 8.1: Regional Context & Routing
**Goal**: Test regional detection and routing functionality

1. **Get Regional Context**
   ```http
   GET /api/v1/regional/context
   ```

2. **Test Regional Routing**
   ```http
   GET /api/v1/regional/test/us
   GET /api/v1/regional/test/eu  
   GET /api/v1/regional/test/asia
   ```

3. **Get Regional Recommendations**
   ```http
   GET /api/v1/regional/products/recommendations
   ```

4. **Region-Specific Product Access**
   ```http
   GET /api/v1/regional/products?page=0&size=20
   X-Region: US
   
   GET /api/v1/regional/products/EU?page=0&size=20
   ```

---

### 9. Internal Controller - Service Integration

#### Scenario 9.1: Service-to-Service Communication
**Goal**: Test internal APIs used by other microservices

1. **Calculate Order Pricing**
   ```http
   POST /api/v1/internal/calculate-pricing
   
   {
     "sagaId": "saga-12345678-abcd-4567-8901-123456789012",
     "customerId": "customer-11111111-1111-4111-8111-111111111111",
     "shopItems": [
       {
         "shopId": "{{shopId}}",
         "items": [
           {
             "productVariantId": "{{variantIPhonePink128Id}}",
             "quantity": 1,
             "requestedPrice": 799.00
           },
           {
             "productVariantId": "{{variantSamsungGray256Id}}",
             "quantity": 1,
             "requestedPrice": 699.00
           }
         ]
       }
     ]
   }
   ```

2. **Health Check**
   ```http
   GET /api/v1/internal/health
   ```

---

## 🔄 End-to-End Business Scenarios

### E-Commerce Customer Journey

#### Complete Purchase Flow
1. **Browse Products** → `POST /api/v1/products/search`
2. **View Product Detail** → `GET /api/v1/products/public/{id}`
3. **Check Availability** → `POST /api/v1/products/availability`
4. **Add to Cart (Reserve)** → `POST /api/v1/products/reserve-inventory`
5. **Calculate Pricing** → `POST /api/v1/internal/calculate-pricing`
6. **Confirm Purchase** → `POST /api/v1/products/inventory-confirmation`

#### Multi-Region Shopping Experience
1. **US Customer** → Search with `region: "US"` and `partitionFirstEnabled: true`
2. **EU Customer** → Search with `region: "EU"` and `partitionFirstEnabled: true`  
3. **ASIA Customer** → Search with `region: "ASIA"` and `partitionFirstEnabled: true`

### Vendor Business Operations

#### Daily Vendor Workflow
1. **Check Dashboard** → `GET /api/v1/vendors/analytics/dashboard`
2. **Review Performance** → `GET /api/v1/vendors/analytics/products/performance`
3. **Update SEO** → `PATCH /api/v1/vendors/products/{id}/seo`
4. **Sync Inventory** → `POST /api/v1/vendors/products/{id}/sync-inventory`
5. **Monitor Stock** → `GET /api/v1/inventories/product/{id}`

### Admin System Management

#### Daily Admin Tasks
1. **System Health** → `GET /api/v1/search/health`
2. **Review Pending** → `GET /api/v1/admin/products/pending-approval`
3. **Approve Products** → `PATCH /api/v1/admin/products/{id}/approve`
4. **Monitor Cache** → `GET /api/v1/admin/system/cache/status`
5. **Elasticsearch Sync** → `POST /api/v1/search/reindex`

---

## 🎯 Performance Testing Scenarios

### High-Volume Search Testing
1. **Concurrent Search Requests** (100+ concurrent users)
2. **Large Result Set Pagination** (page through 1000+ results)
3. **Complex Filter Combinations** (multiple filters + sorts)
4. **Partition-First Optimization** (measure latency differences)

### Inventory Stress Testing
1. **Concurrent Reservations** (same product, multiple users)
2. **Bulk Availability Checks** (100+ SKUs)
3. **Rapid Reserve/Release Cycles** (cart abandonment simulation)

### Elasticsearch Performance
1. **Index Health Monitoring** → `GET /api/v1/search/health`
2. **Document Count Tracking** → `GET /api/v1/search/stats`
3. **Reindex Performance** → `POST /api/v1/search/reindex`

---

## 🔒 Security Testing Scenarios

### Authentication & Authorization
1. **Public Endpoints** (no auth required)
2. **Customer Endpoints** (customer token required)
3. **Vendor Endpoints** (vendor token required)
4. **Admin Endpoints** (admin token required)

### Data Access Control
1. **Vendor Isolation** (vendor can only access own products)
2. **Regional Data Access** (region-specific data filtering)
3. **Cross-Vendor Access** (should be blocked)

---

## 📊 Monitoring & Validation

### Key Metrics to Track
- **Search Response Time** (target: <200ms)
- **Inventory Reservation Success Rate** (target: >99%)
- **Elasticsearch Index Health** (green status)
- **Cache Hit Rates** (target: >80%)
- **Regional Search Accuracy** (correct partition routing)

### Validation Checkpoints
- ✅ All seed data properly created
- ✅ Elasticsearch fully synchronized  
- ✅ Regional filtering working correctly
- ✅ Inventory reservations accurate
- ✅ Price calculations correct
- ✅ SEO metadata properly stored
- ✅ Cache eviction working
- ✅ Authentication enforced correctly

---

## 🚀 Getting Started

1. **Start the Product Service** with all dependencies
2. **Import Environment File** with all seed data UUIDs
3. **Import Postman Collection** with 70+ requests
4. **Run DatabaseInitializer** to create seed data
5. **Verify Elasticsearch Sync** using health endpoints
6. **Execute Test Scenarios** in sequence
7. **Monitor Results** and validate expected behavior

This comprehensive testing guide ensures thorough validation of all Product Service functionality across multiple regions, user roles, and business scenarios. 
