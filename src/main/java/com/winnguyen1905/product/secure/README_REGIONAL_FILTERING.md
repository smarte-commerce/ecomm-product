# Regional Filtering System Documentation

## Overview

This document explains how the smart regional filtering system works in the Product Service, automatically isolating data by geographic regions using the user's detected location.

## System Components

### 1. 🌐 **AccountRequestArgumentResolver**
**Location**: `src/main/java/com/winnguyen1905/product/secure/AccountRequestArgumentResolver.java`

**Purpose**: Automatically detects user's region from multiple sources and sets it in thread-local context.

**Detection Priority Order**:
1. **Redis IP-region cache** (Primary - cached by gateway)
2. **Gateway headers** (`X-Region-Code`)
3. **JWT token region** 
4. **Session-based cache**
5. **Accept-Language header** analysis
6. **Legacy IP detection**
7. **Default fallback** (US)

**Key Code**:
```java
// Sets region in thread-local context for use by other components
RegionalDataSourceConfiguration.RegionalContext.setCurrentRegion(region);
```

### 2. 🔧 **RegionHibernateFilterConfigurer**
**Location**: `src/main/java/com/winnguyen1905/product/secure/RegionHibernateFilterConfigurer.java`

**Purpose**: Automatically enables Hibernate filters based on the region set by AccountRequestArgumentResolver.

**How it Works**:
- Runs as a Servlet filter for every request
- Gets region from thread-local context
- Enables Hibernate `regionFilter` for the current session
- Automatically applies to all queries in the request

**Key Code**:
```java
RegionPartition region = RegionalDataSourceConfiguration.RegionalContext.getCurrentRegion();
if (region != null) {
    session.enableFilter("regionFilter").setParameter("region", region.getCode());
}
```

### 3. 🗃️ **Entity Filters**
**Entities**: `EProduct`, `EBrand`, `ECategory`, `EProductVariant`, etc.

**Filter Definitions**:
```java
@FilterDef(name = "regionFilter", parameters = @ParamDef(name = "region", type = String.class))
@Filter(name = "regionFilter", condition = "region = :region")
```

**Effect**: When enabled, ALL queries to these entities automatically include `WHERE region = 'us'` (or eu/asia).

### 4. 🏗️ **RegionalDataSourceConfiguration**
**Location**: `src/main/java/com/winnguyen1905/product/config/RegionalDataSourceConfiguration.java`

**Purpose**: Routes database connections to regional clusters AND provides thread-local context management.

**Database Routing**:
- **US**: `cockroachdb-1:26257/ecommerce_us`
- **EU**: `cockroachdb-2:26257/ecommerce_eu` 
- **ASIA**: `cockroachdb-3:26257/ecommerce_asia`

### 5. 🔍 **RegionalQueryService**
**Location**: `src/main/java/com/winnguyen1905/product/service/RegionalQueryService.java`

**Purpose**: Demonstrates advanced regional query patterns and provides utilities for manual filter control.

**Key Methods**:
```java
// Automatic region filtering (uses current user's region)
findProductsInCurrentRegion(pageable)

// Specific region filtering (override current region)
findProductsInSpecificRegion(region, pageable)

// Bypass region filtering (admin queries)
findProductsInAllRegions(pageable)

// Manual filter control
withRegionFilter(region, () -> { /* query code */ })
withoutRegionFilter(() -> { /* query code */ })
```

## How It All Works Together

### 🔄 **Request Flow**

1. **Client Request** → Gateway Service
2. **Gateway** detects region from IP, caches in Redis
3. **Request** forwarded to Product Service with headers
4. **AccountRequestArgumentResolver** extracts region, sets in thread-local
5. **RegionHibernateFilterConfigurer** enables filters based on region
6. **All Repository Queries** automatically filtered by region
7. **Response** contains only data from user's region

### 📊 **Query Behavior Examples**

#### Automatic Filtering (Default Behavior)
```java
@GetMapping("/products")
public Page<EProduct> getProducts(@AccountRequest TAccountRequest account, Pageable pageable) {
    // This query automatically includes: WHERE region = 'us' (or user's region)
    return productRepository.findAll(pageable);
}
```

**Generated SQL**:
```sql
SELECT * FROM products WHERE region = 'us' AND is_deleted = false ORDER BY created_date DESC;
```

#### Manual Region Override
```java
public Page<EProduct> getEuProducts(Pageable pageable) {
    return regionalQueryService.withRegionFilter(RegionPartition.EU, () -> {
        return productRepository.findAll(pageable);
    });
}
```

#### Cross-Region Queries (Admin)
```java
public Page<EProduct> getAllProducts(Pageable pageable) {
    return regionalQueryService.withoutRegionFilter(() -> {
        return productRepository.findAll(pageable);
    });
}
```

## 📋 **Implementation Examples**

### Simple Controller Endpoint
```java
@GetMapping("/products")
public ResponseEntity<Page<EProduct>> getProducts(
        @AccountRequest TAccountRequest account,
        Pageable pageable) {
    
    // Automatically filtered by user's region (account.region())
    Page<EProduct> products = productRepository.findAll(pageable);
    return ResponseEntity.ok(products);
}
```

### Custom Repository Query
```java
@Query("SELECT p FROM EProduct p WHERE p.isPublished = true ORDER BY p.viewCount DESC")
Page<EProduct> findPopularProducts(Pageable pageable);
// ↑ Automatically includes: AND region = :region
```

### Complex Service Logic
```java
@Service
public class ProductService {
    
    public List<EProduct> findRelatedProducts(UUID productId) {
        // This will automatically be filtered by user's region
        return productRepository.findByCategoryId(category.getId());
    }
    
    public RegionalStats getAdminStats() {
        // Bypass region filtering for admin analytics
        return regionalQueryService.withoutRegionFilter(() -> {
            return calculateCrossRegionStats();
        });
    }
}
```

## 🚀 **Benefits**

### **Automatic Data Isolation**
- ✅ No manual region checks in business logic
- ✅ Prevents data leakage between regions  
- ✅ Consistent filtering across all queries

### **Performance Optimization**
- ✅ Queries only search relevant regional data
- ✅ Better query performance with smaller datasets
- ✅ Regional database routing reduces latency

### **Developer Experience**
- ✅ Transparent to developers - works automatically
- ✅ Override mechanisms when needed
- ✅ Clear debugging tools and monitoring

### **Security & Compliance**
- ✅ Data residency compliance (GDPR, etc.)
- ✅ Automatic regional data isolation
- ✅ Audit trail of regional access patterns

## 🔧 **Configuration**

### Enable Regional Filtering
Regional filtering is **enabled by default** for all requests. No additional configuration needed.

### Disable for Specific Endpoints
```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/admin/") || path.startsWith("/actuator/");
}
```

### Debug Region Detection
```bash
# Check region detection
curl -H "X-Forwarded-For: 203.0.113.1" http://localhost:8080/api/v1/regional/debug/region-info
```

## 🐛 **Troubleshooting**

### Issue: No Products Returned
**Cause**: Region filter active but no products in user's region
**Debug**: Check `/debug/region-info` endpoint
**Solution**: Add products to correct region or verify region detection

### Issue: Cross-Region Data Visible
**Cause**: Region filter not enabled
**Debug**: Check filter status in logs
**Solution**: Ensure RegionHibernateFilterConfigurer is registered

### Issue: Wrong Region Detected  
**Cause**: IP geolocation error or cache issues
**Debug**: Check Redis cache and gateway logs
**Solution**: Clear Redis cache or add manual region header

## 📝 **Best Practices**

### **DO** ✅
- Use `@AccountRequest TAccountRequest` to get user context
- Let filters work automatically for most queries
- Use `RegionalQueryService` for advanced patterns
- Test with different region scenarios

### **DON'T** ❌
- Add manual `WHERE region =` clauses in queries
- Disable filters without clear admin use case
- Assume region without checking account context
- Bypass security for non-admin operations

## 🔍 **Monitoring & Metrics**

### Region Distribution Monitoring
```java
@GetMapping("/admin/stats")
public ResponseEntity<RegionalStats> getStats() {
    return ResponseEntity.ok(regionalQueryService.getRegionalProductStats());
}
```

### Cache Hit Rate Monitoring
```bash
# Redis region cache metrics
redis-cli INFO keyspace
redis-cli KEYS "ip:region:*" | wc -l
```

### Query Performance by Region
Check slow query logs in each regional database cluster for optimization opportunities.

---

**🎯 This system provides transparent, automatic regional data isolation while maintaining developer productivity and system performance.** 
