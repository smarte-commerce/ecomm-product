# Product Service API - Endpoints Summary

This document provides a comprehensive overview of all API endpoints included in the Postman collection.

## Overview

- **Total Controllers**: 12
- **Total Folders**: 12
- **Total Endpoints**: 80+
- **Authentication**: JWT Bearer Token
- **Base URL**: `http://localhost:8080`

---

## 1. Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | User login | No |

---

## 2. Product CRUD (`/api/v1/products`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/products` | Create new product | VENDOR/ADMIN |
| GET | `/api/v1/products/{productId}` | Get product by ID | Optional |
| GET | `/api/v1/products/public/{productId}` | Get public product | No |
| PUT | `/api/v1/products/{productId}` | Update product | VENDOR/ADMIN |
| DELETE | `/api/v1/products/{productId}` | Delete product | VENDOR/ADMIN |
| PATCH | `/api/v1/products/{productId}/restore` | Restore deleted product | VENDOR/ADMIN |

---

## 3. Product Search (`/api/v1/products/search`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/products/search` | Advanced search | Optional |
| GET | `/api/v1/products/search/popular` | Get popular products | No |
| GET | `/api/v1/products/search/related/{productId}` | Get related products | No |
| GET | `/api/v1/products/search/category/{categoryId}` | Get products by category | Optional |
| GET | `/api/v1/products/search/brand/{brandId}` | Get products by brand | Optional |
| GET | `/api/v1/products/search/slug/{slug}` | Get product by slug | No |

---

## 4. Categories (`/api/v1/categories`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/categories` | Get all categories | No |
| GET | `/api/v1/categories/{id}` | Get category by ID | No |
| POST | `/api/v1/categories` | Create category | ADMIN |
| PUT | `/api/v1/categories/{id}` | Update category | ADMIN |
| DELETE | `/api/v1/categories/{id}` | Delete category | ADMIN |
| GET | `/api/v1/categories/tree` | Get category tree | No |
| POST | `/api/v1/categories/{id}/move` | Move category | ADMIN |
| GET | `/api/v1/categories/search` | Search categories | No |
| GET | `/api/v1/categories/{id}/products` | Get category products | No |

---

## 5. Brands (`/api/v1/brands`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/brands` | Get all brands | No |
| GET | `/api/v1/brands/{id}` | Get brand by ID | No |
| POST | `/api/v1/brands` | Create brand | VENDOR/ADMIN |
| PUT | `/api/v1/brands/{id}` | Update brand | VENDOR/ADMIN |
| DELETE | `/api/v1/brands/{id}` | Delete brand | ADMIN |
| GET | `/api/v1/brands/vendor/{vendorId}` | Get vendor brands | VENDOR/ADMIN |
| GET | `/api/v1/brands/search` | Search brands | No |

---

## 6. Customer Products (`/api/v1/customer/products`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/customer/products/search` | Search products | No |
| GET | `/api/v1/customer/products/{id}` | Get product detail | No |
| GET | `/api/v1/customer/products/{productId}/images` | Get product images | No |
| GET | `/api/v1/customer/products/{productId}/variants` | Get product variants | No |
| POST | `/api/v1/customer/products/availability` | Check availability | No |
| POST | `/api/v1/customer/products/reserve-inventory` | Reserve inventory | No |
| POST | `/api/v1/customer/products/inventory-confirmation` | Confirm inventory | No |
| GET | `/api/v1/customer/products/variant-details/{ids}` | Get variant details | No |

---

## 7. Inventory Management (`/api/v1/inventories`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/inventories/product/{productId}` | Get product inventories | VENDOR/ADMIN |
| GET | `/api/v1/inventories/{id}` | Get inventory by ID | VENDOR/ADMIN |
| GET | `/api/v1/inventories/sku/{sku}` | Get inventory by SKU | VENDOR/ADMIN/CUSTOMER |
| PUT | `/api/v1/inventories/{id}` | Update inventory | VENDOR/ADMIN |
| PATCH | `/api/v1/inventories/{id}/reserve` | Reserve inventory | VENDOR/ADMIN/CUSTOMER |
| PATCH | `/api/v1/inventories/{id}/release` | Release inventory | VENDOR/ADMIN |
| POST | `/api/v1/inventories/check-availability` | Check availability | No |
| POST | `/api/v1/inventories/reserve-batch` | Batch reserve | CUSTOMER/ADMIN |

---

## 8. Product Images (`/api/v1/products/{productId}/images`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/products/{productId}/images` | Get product images | No |
| GET | `/api/v1/products/images/{imageId}` | Get image by ID | No |
| POST | `/api/v1/products/{productId}/images` | Upload image | VENDOR/ADMIN |
| PUT | `/api/v1/products/images/{imageId}` | Update image metadata | VENDOR/ADMIN |
| DELETE | `/api/v1/products/images/{imageId}` | Delete image | VENDOR/ADMIN |
| POST | `/api/v1/products/images/bulk` | Bulk upload images | VENDOR/ADMIN |
| PATCH | `/api/v1/products/images/{imageId}/primary` | Set primary image | VENDOR/ADMIN |

---

## 9. Vendor Management (`/api/v1/products/vendor` & `/api/v1/vendors`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/products/vendor/{vendorId}` | Get vendor products | VENDOR/ADMIN |
| GET | `/api/v1/products/vendor/shop/{shopId}` | Get shop products | No |
| GET | `/api/v1/products/vendor/analytics/{vendorId}` | Get vendor stats | VENDOR/ADMIN |
| GET | `/api/v1/products/vendor/low-stock` | Get low stock products | VENDOR/ADMIN |
| PATCH | `/api/v1/products/vendor/{productId}/seo` | Update SEO metadata | VENDOR/ADMIN |
| POST | `/api/v1/products/vendor/{productId}/sync-inventory` | Sync inventory | VENDOR/ADMIN |
| POST | `/api/v1/vendors/register` | Register vendor | No |
| GET | `/api/v1/vendors/profile` | Get vendor profile | VENDOR |
| PUT | `/api/v1/vendors/profile` | Update vendor profile | VENDOR |
| GET | `/api/v1/vendors/analytics/dashboard` | Get vendor dashboard | VENDOR |
| GET | `/api/v1/vendors/analytics/products/performance` | Get product performance | VENDOR |
| GET | `/api/v1/vendors/settings` | Get vendor settings | VENDOR |
| PUT | `/api/v1/vendors/settings` | Update vendor settings | VENDOR |
| POST | `/api/v1/vendors/verification/documents` | Upload verification docs | VENDOR |
| GET | `/api/v1/vendors/verification/status` | Get verification status | VENDOR |

---

## 10. Bulk Operations (`/api/v1/products/bulk`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| PATCH | `/api/v1/products/bulk/status` | Bulk update status | VENDOR/ADMIN |
| PATCH | `/api/v1/products/bulk/publish` | Bulk publish/unpublish | VENDOR/ADMIN |
| DELETE | `/api/v1/products/bulk` | Bulk delete products | VENDOR/ADMIN |
| POST | `/api/v1/products/bulk/import` | Bulk import products | VENDOR/ADMIN |

---

## 11. Admin Operations (`/api/v1/admin`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/admin/inventories` | Get all inventories | ADMIN |
| GET | `/api/v1/admin/inventories/{inventoryId}` | Get inventory by ID | ADMIN |
| DELETE | `/api/v1/admin/inventories/{inventoryId}` | Delete inventory | ADMIN |
| GET | `/api/v1/admin/products/pending-approval` | Get pending products | ADMIN |
| PATCH | `/api/v1/admin/products/{productId}/approve` | Approve product | ADMIN |
| GET | `/api/v1/admin/system/cache/status` | Get cache status | ADMIN |
| POST | `/api/v1/admin/system/cache/clear` | Clear cache | ADMIN |

---

## 12. Elasticsearch (`/api/v1/elasticsearch`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/elasticsearch/search` | Search products | No |
| GET | `/api/v1/elasticsearch/suggestions` | Get search suggestions | No |
| POST | `/api/v1/elasticsearch/sync/product/{productId}` | Sync product | ADMIN |
| POST | `/api/v1/elasticsearch/sync/products` | Sync multiple products | ADMIN |
| POST | `/api/v1/elasticsearch/sync/vendor/{vendorId}` | Sync vendor products | ADMIN/VENDOR |
| POST | `/api/v1/elasticsearch/reindex` | Full reindex | ADMIN |
| POST | `/api/v1/elasticsearch/sync/inventory/{productId}` | Sync inventory | ADMIN/VENDOR |
| DELETE | `/api/v1/elasticsearch/product/{productId}` | Delete from index | ADMIN |
| GET | `/api/v1/elasticsearch/health` | Check index health | No |
| GET | `/api/v1/elasticsearch/stats` | Get index statistics | ADMIN |

---

## 13. Cache Management (`/api/v1/products/cache`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/products/cache/evict/{productId}` | Evict product cache | ADMIN |
| POST | `/api/v1/products/cache/warmup` | Warm up cache | ADMIN |

---

## Request Body Examples

### Create Product Request
```json
{
  "name": "Sample Product",
  "description": "Product description",
  "productType": "SIMPLE",
  "vendorId": "uuid",
  "shopId": "uuid",
  "region": "US",
  "basePrice": 29.99,
  "variants": [
    {
      "sku": "SAMPLE-001",
      "price": 29.99,
      "inventoryQuantity": 100
    }
  ]
}
```

### Search Request
```json
{
  "keyword": "electronics",
  "pagination": {
    "pageNum": 0,
    "pageSize": 20
  },
  "filters": [
    {
      "field": "category",
      "values": ["electronics"],
      "operator": "in"
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

### Vendor Registration
```json
{
  "businessName": "Sample Business",
  "businessType": "CORPORATION",
  "contactPersonName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "businessAddress": {
    "streetAddress": "123 Business St",
    "city": "Business City",
    "stateProvince": "BC",
    "postalCode": "12345",
    "countryCode": "US"
  },
  "bankingInformation": {
    "bankName": "Sample Bank",
    "accountHolderName": "John Doe",
    "accountNumber": "123456789012",
    "routingNumber": "123456789",
    "accountType": "BUSINESS"
  },
  "region": "US",
  "acceptedTermsAndConditions": true,
  "acceptedPrivacyPolicy": true
}
```

---

## Authentication Roles

- **PUBLIC** - No authentication required
- **CUSTOMER** - Standard customer access
- **VENDOR** - Vendor account with product management rights
- **ADMIN** - Administrative access to all resources

---

## HTTP Status Codes

- **200** - Success
- **201** - Created
- **204** - No Content
- **400** - Bad Request
- **401** - Unauthorized
- **403** - Forbidden
- **404** - Not Found
- **409** - Conflict
- **422** - Unprocessable Entity
- **500** - Internal Server Error

---

This collection provides comprehensive coverage of the Product Service API, enabling testing of all major functionality including product management, inventory control, search capabilities, vendor operations, and administrative functions. 
