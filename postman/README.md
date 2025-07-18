# Product Service API - Comprehensive Testing Suite

## 🎯 Overview

This comprehensive testing suite provides complete API coverage for the Product Service with **real seed data integration**. The DatabaseInitializer creates a rich, multi-regional dataset that powers realistic testing scenarios across all controllers and business workflows.

## 📦 What's Included

### 🗃️ Seed Data (Auto-Generated)
- **6 Brands** across all regions (Nike, Adidas, Apple, Samsung, IKEA, Sony)
- **8 Categories** with hierarchical structure (Electronics/Smartphones/Laptops, Fashion/Shoes/Clothing, Furniture/Home Office)
- **6 Products** with 14 variants total across multiple regions
- **Complete Inventory** data with regional warehouses
- **24 Product Images** with CDN metadata
- **Automatic Elasticsearch Sync** for search functionality

### 📚 API Collections
- **Product-Service-Complete.postman_collection.json** - 70+ requests covering all controllers
- **Product-Service-Environment.postman_environment.json** - 40+ variables with exact seed data UUIDs
- **TESTING_SCENARIOS.md** - Comprehensive testing guide with business workflows
- **Part 2 Collection** - Additional controllers and advanced scenarios

### 🎮 Controllers Covered (10 Total)
1. **Product Controller** - Core CRUD operations
2. **Search Controller** - Elasticsearch with partition-first optimization  
3. **Brand Controller** - Brand management and discovery
4. **Category Controller** - Hierarchical navigation
5. **Vendor Controller** - Business operations and analytics
6. **Admin Controller** - System management and bulk operations
7. **Inventory Controller** - Stock management and reservations
8. **Internal Controller** - Service-to-service communication
9. **Regional Controller** - Multi-region functionality
10. **Business Scenarios** - End-to-end workflows

---

## 🚀 Quick Start

### 1. Prerequisites
- **Product Service** running on `http://localhost:8080`
- **Elasticsearch** running and accessible
- **Database** connected and initialized
- **Postman** or any API client

### 2. Setup Environment
1. **Import Environment File**:
   ```
   product/postman/Product-Service-Environment.postman_environment.json
   ```

2. **Import Collection**:
   ```
   product/postman/Product-Service-Complete.postman_collection.json
   ```

3. **Verify Seed Data** (automatic on startup):
   - DatabaseInitializer runs on application start
   - Creates 6 brands, 8 categories, 6 products, 14 variants
   - Syncs everything to Elasticsearch automatically

### 3. Authentication Tokens
Set these in your environment:
- `{{adminToken}}` - For admin operations
- `{{vendorToken}}` - For vendor operations  
- `{{customerToken}}` - For customer operations

### 4. Validate Setup
Run these health checks:
```http
GET {{baseUrl}}/api/v1/search/health
GET {{baseUrl}}/api/v1/internal/health
GET {{baseUrl}}/api/v1/categories
```

---

## 🗂️ Seed Data Reference

### Products with Exact UUIDs

#### iPhone 15 (US Region)
- **Product ID**: `03000000-0000-4000-8000-000000000001`
- **Variants**:
  - Pink 128GB: `04000000-0000-4000-8000-000000000001`
  - Blue 256GB: `04000000-0000-4000-8000-000000000002`
  - Black 512GB: `04000000-0000-4000-8000-000000000003`

#### Samsung Galaxy S24 (ASIA Region)
- **Product ID**: `03000000-0000-4000-8000-000000000002`
- **Variants**:
  - Titanium Gray 256GB: `04000000-0000-4000-8000-000000000004`
  - Violet 512GB: `04000000-0000-4000-8000-000000000005`

#### Nike Air Max 270 (US Region)
- **Product ID**: `03000000-0000-4000-8000-000000000003`
- **Variants**:
  - Black US 9: `04000000-0000-4000-8000-000000000006`
  - White US 10: `04000000-0000-4000-8000-000000000007`
  - Red US 11: `04000000-0000-4000-8000-000000000008`

#### Adidas Ultraboost 22 (EU Region)
- **Product ID**: `03000000-0000-4000-8000-000000000004`
- **Variants**:
  - Core Black EU 42: `04000000-0000-4000-8000-000000000009`
  - White EU 43: `04000000-0000-4000-8000-000000000010`

#### IKEA BEKANT Desk (ASIA Region)
- **Product ID**: `03000000-0000-4000-8000-000000000005`
- **Variants**:
  - White 120x80cm: `04000000-0000-4000-8000-000000000011`
  - Black-brown 160x80cm: `04000000-0000-4000-8000-000000000012`

#### Sony WH-1000XM5 (ASIA Region)
- **Product ID**: `03000000-0000-4000-8000-000000000006`
- **Variants**:
  - Black: `04000000-0000-4000-8000-000000000013`
  - Silver: `04000000-0000-4000-8000-000000000014`

---

## 🧪 Testing Workflows

### 🛒 E-Commerce Customer Journey
**Test the complete shopping experience**

1. **Product Discovery**
   ```http
   POST {{baseUrl}}/api/v1/products/search
   Body: {"keyword": "iPhone", "pagination": {"pageNum": 0, "pageSize": 12}}
   ```

2. **Product Details**
   ```http
   GET {{baseUrl}}/api/v1/products/public/{{productIPhone15Id}}
   ```

3. **Stock Check**
   ```http
   POST {{baseUrl}}/api/v1/products/availability
   Body: {"productVariantIds": ["{{variantIPhonePink128Id}}"], "quantities": [1]}
   ```

4. **Add to Cart**
   ```http
   POST {{baseUrl}}/api/v1/products/reserve-inventory
   Body: {"customerId": "{{testCustomerId}}", "items": [{"productVariantId": "{{variantIPhonePink128Id}}", "quantity": 1}]}
   ```

### 🌍 Multi-Region Testing
**Test regional optimization and data distribution**

1. **US Region Search**
   ```http
   POST {{baseUrl}}/api/v1/search/elasticsearch
   Headers: X-Region: US
   Body: {"keyword": "iPhone", "region": "US", "partitionFirstEnabled": true}
   ```

2. **EU Region Search**
   ```http
   POST {{baseUrl}}/api/v1/search/elasticsearch
   Headers: X-Region: EU
   Body: {"keyword": "Adidas", "region": "EU", "partitionFirstEnabled": true}
   ```

3. **ASIA Region Search**
   ```http
   POST {{baseUrl}}/api/v1/search/elasticsearch
   Headers: X-Region: ASIA
   Body: {"keyword": "Samsung", "region": "ASIA", "partitionFirstEnabled": true}
   ```

### 🏪 Vendor Business Operations
**Test vendor dashboard and management features**

1. **Vendor Dashboard**
   ```http
   GET {{baseUrl}}/api/v1/vendors/analytics/dashboard
   Authorization: Bearer {{vendorToken}}
   ```

2. **Product Performance**
   ```http
   GET {{baseUrl}}/api/v1/vendors/analytics/products/performance?days=30
   Authorization: Bearer {{vendorToken}}
   ```

3. **SEO Optimization**
   ```http
   PATCH {{baseUrl}}/api/v1/vendors/products/{{productIPhone15Id}}/seo
   Authorization: Bearer {{vendorToken}}
   Body: {"metaTitle": "iPhone 15 - Best Smartphone 2024", "metaDescription": "Advanced camera system..."}
   ```

### 🔧 Admin System Management
**Test administrative functions and monitoring**

1. **System Monitoring**
   ```http
   GET {{baseUrl}}/api/v1/search/health
   GET {{baseUrl}}/api/v1/search/stats
   Authorization: Bearer {{adminToken}}
   ```

2. **Bulk Operations**
   ```http
   PATCH {{baseUrl}}/api/v1/admin/products/bulk/status
   Authorization: Bearer {{adminToken}}
   Body: {"productIds": ["{{productIPhone15Id}}", "{{productSamsungS24Id}}"], "status": "ACTIVE"}
   ```

3. **Cache Management**
   ```http
   GET {{baseUrl}}/api/v1/admin/system/cache/status
   POST {{baseUrl}}/api/v1/admin/system/cache/clear
   Authorization: Bearer {{adminToken}}
   ```

---

## 🔍 Advanced Testing Scenarios

### Elasticsearch Integration
- **Full Reindex**: `POST /api/v1/search/reindex`
- **Product Sync**: `POST /api/v1/search/sync/product/{id}`
- **Bulk Sync**: `POST /api/v1/search/sync/products`
- **Health Check**: `GET /api/v1/search/health`

### Inventory Management
- **Stock Tracking**: `GET /api/v1/inventories/product/{id}`
- **SKU Lookup**: `GET /api/v1/inventories/sku/{sku}`
- **Reservations**: `PATCH /api/v1/inventories/reserve`
- **Batch Availability**: `POST /api/v1/inventories/check-availability`

### Regional Operations
- **Context Detection**: `GET /api/v1/regional/context`
- **Routing Tests**: `GET /api/v1/regional/test/{region}`
- **Recommendations**: `GET /api/v1/regional/products/recommendations`

### Service Integration
- **Pricing Calculation**: `POST /api/v1/internal/calculate-pricing`
- **Health Monitoring**: `GET /api/v1/internal/health`

---

## 📊 Performance Benchmarks

### Expected Response Times
- **Product Search**: < 200ms
- **Product Details**: < 100ms
- **Inventory Check**: < 50ms
- **Category Browse**: < 150ms
- **Brand Listing**: < 100ms

### Elasticsearch Metrics
- **Index Health**: Green status
- **Document Count**: 14 variants indexed
- **Search Latency**: < 50ms average
- **Reindex Time**: < 10 seconds

### Regional Performance
- **Partition-First Search**: 30-50% faster for regional queries
- **Cross-Region Fallback**: < 500ms total
- **Regional Context**: < 10ms detection

---

## 🛡️ Security Testing

### Authentication Levels
- **Public**: No authentication required
- **Customer**: Customer token validation
- **Vendor**: Vendor-specific access control
- **Admin**: Full system access

### Authorization Tests
- **Vendor Isolation**: Vendors can only access own products
- **Regional Access**: Proper regional data filtering
- **Admin Override**: Admin access to all resources
- **Cross-Vendor Protection**: Blocked unauthorized access

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Seed Data Not Created
**Symptom**: Empty responses from GET requests
**Solution**: 
```bash
# Check if DatabaseInitializer ran
curl http://localhost:8080/api/v1/categories
# Should return 8 categories

# Restart application if needed
./mvnw spring-boot:run
```

#### 2. Elasticsearch Not Synced
**Symptom**: Search returns no results
**Solution**:
```http
POST {{baseUrl}}/api/v1/search/reindex
# Wait 10-30 seconds for reindexing

GET {{baseUrl}}/api/v1/search/health
# Should return healthy status
```

#### 3. Authentication Errors
**Symptom**: 401/403 responses
**Solution**:
- Verify tokens are set in environment
- Check token expiration
- Ensure correct role-based access

#### 4. Regional Routing Issues
**Symptom**: Wrong regional data returned
**Solution**:
```http
GET {{baseUrl}}/api/v1/regional/context
# Verify region detection

GET {{baseUrl}}/api/v1/regional/test/us
# Test specific region routing
```

---

## 📈 Monitoring & Analytics

### Key Metrics to Track
1. **API Response Times** across all endpoints
2. **Elasticsearch Index Health** and performance
3. **Inventory Reservation Success Rate**
4. **Regional Search Accuracy**
5. **Cache Hit Rates** for frequently accessed data

### Health Check Endpoints
```http
GET {{baseUrl}}/api/v1/search/health          # Elasticsearch status
GET {{baseUrl}}/api/v1/internal/health        # Service health
GET {{baseUrl}}/api/v1/search/stats           # Index statistics
GET {{baseUrl}}/api/v1/admin/system/cache/status  # Cache status
```

---

## 🤝 Contributing

### Adding New Test Scenarios
1. **Identify Business Use Case**
2. **Create Request in Collection**
3. **Use Seed Data UUIDs**
4. **Document in TESTING_SCENARIOS.md**
5. **Add Performance Benchmarks**

### Environment Variables
- Use descriptive names with seed data references
- Include all necessary IDs for comprehensive testing
- Document any new variables added

### Best Practices
- Always use seed data UUIDs for consistency
- Include authentication headers where required
- Add meaningful descriptions to requests
- Test both success and error scenarios

---

## 📚 Additional Resources

- **TESTING_SCENARIOS.md** - Detailed testing workflows
- **ENDPOINTS_SUMMARY.md** - Complete API reference
- **DatabaseInitializer.java** - Seed data creation logic
- **API Documentation** - Swagger/OpenAPI specs

## 📞 Support

For issues related to:
- **API Functionality**: Check controller implementations
- **Seed Data**: Review DatabaseInitializer configuration
- **Elasticsearch**: Verify index health and sync status
- **Regional Features**: Test regional context endpoints
- **Performance**: Monitor health check endpoints

---

**Ready to test?** Import the collections, verify your environment variables, and start exploring the comprehensive Product Service API with real, rich seed data! 
