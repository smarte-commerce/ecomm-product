# Product Service

## Overview

The Product Service is the central component for managing the product catalog, inventory, and search capabilities. It provides comprehensive APIs for creating, updating, searching, and managing products with support for regional data distribution, advanced search features via Elasticsearch, and multi-vendor operations.

## Key Features

- **Complete Product Lifecycle Management**: Create, read, update, delete products with versioning
- **Regional Product Data**: Support for region-specific product catalogs (US, EU, ASIA)
- **Advanced Search**: Elasticsearch integration for performant, feature-rich search functionality
- **Multi-vendor Architecture**: Vendor-specific product management
- **Product Variants**: Support for multiple variants (size, color, etc.) per product
- **Rich Media Management**: Images, videos, and documents associated with products
- **SEO Optimization**: SEO-friendly URLs, metadata, and content management
- **Caching**: Multi-level caching with Redis and Caffeine
- **Price Management**: Base price, sale price, currency handling
- **Regional Gateway Integration**: Enhanced routing based on user location

## Technical Stack

- Spring Boot 3.x
- Spring Data JPA
- Spring Cloud (Eureka, OpenFeign, Gateway integration)
- Elasticsearch for search capabilities
- Redis for distributed caching
- PostgreSQL/CockroachDB for data persistence
- Resilience4j for fault tolerance

## Project Structure

- `/src/main/java/com/winnguyen1905/product/`
  - `/core/`: Core business logic
    - `/controller/`: REST controllers
    - `/model/`: Domain models
    - `/service/`: Service interfaces and implementations
  - `/config/`: Configuration classes
  - `/exception/`: Exception handling
  - `/persistence/`: Data access layer
    - `/entity/`: JPA entities
    - `/repository/`: JPA repositories
  - `/security/`: Security configuration
  - `/validation/`: Validation utilities
  - `/elasticsearch/`: Elasticsearch configuration and repositories
  - `/mapper/`: DTO-Entity mapping utilities
  - `/util/`: Utility classes

## API Endpoints

### Product Management
- `GET /api/v1/products`: List products with filtering, sorting, and pagination
- `GET /api/v1/products/{id}`: Get product by ID
- `POST /api/v1/products`: Create new product
- `PUT /api/v1/products/{id}`: Update existing product
- `DELETE /api/v1/products/{id}`: Delete product (soft delete)
- `GET /api/v1/products/vendor/{vendorId}`: Get products by vendor
- `GET /api/v1/products/search`: Search products (Elasticsearch)

### Product Variants
- `GET /api/v1/products/{productId}/variants`: Get all variants for a product
- `POST /api/v1/products/{productId}/variants`: Add variant to product
- `PUT /api/v1/products/{productId}/variants/{variantId}`: Update product variant
- `DELETE /api/v1/products/{productId}/variants/{variantId}`: Delete product variant

### Product Categories
- `GET /api/v1/categories`: List all categories
- `GET /api/v1/categories/{id}`: Get category by ID
- `POST /api/v1/categories`: Create new category
- `PUT /api/v1/categories/{id}`: Update category
- `DELETE /api/v1/categories/{id}`: Delete category
- `GET /api/v1/categories/{id}/products`: Get products by category

### Regional Endpoints
- `GET /api/v1/regional/context`: Get current regional context
- `GET /api/v1/regional/products/recommendations`: Get region-specific product recommendations

## Recent Updates

### Product Service Integration with Enhanced Gateway

The Product Service has been enhanced to integrate with the Gateway's regional routing capabilities. Key changes include:

1. **New Components**:
   - `RegionalContextService`: Extracts gateway headers for regional context
   - `RegionalProductController`: Demonstrates regional functionality

2. **Enhanced Components**:
   - `AccountRequestArgumentResolver`: Added gateway header parsing
   - `RegionPartition`: Enhanced with utility methods
   - `DatabaseInitializer`: Creates regional product data

3. **Header Integration**:
   - Primary region determination from `X-Region-Code` header
   - Fallback to JWT token region claims
   - Support for region-specific timezones

See [PRODUCT_SERVICE_INTEGRATION_SUMMARY.md](../PRODUCT_SERVICE_INTEGRATION_SUMMARY.md) for more details.

### Elasticsearch Refactoring

The Product Service's search capabilities have been enhanced through Elasticsearch refactoring:

1. **Custom Analyzers**: Added language-specific analyzers
2. **Advanced Query Builder**: Enhanced query capabilities with fuzzy matching
3. **Performance Optimizations**: Bulk indexing, shard configuration
4. **Scalability Improvements**: Document structure optimization, indexing strategy

See [ELASTICSEARCH_REFACTOR.md](./ELASTICSEARCH_REFACTOR.md) for more details.

### Product Service Refactoring

Consolidated duplicate product creation methods and simplified service folder structure:

1. **Service Interface Refactoring**: Removed duplicate methods
2. **Enhanced Integration**: Added Elasticsearch operations integration
3. **Controller Documentation**: Updated with comprehensive documentation
4. **Service Folder Structure Simplification**: Flattened hierarchy

See [REFACTORING_SUMMARY.md](./REFACTORING_SUMMARY.md) for more details.

## Getting Started

### Prerequisites
- Java 21
- Maven 3.8+
- Docker and Docker Compose (for running dependencies)
- PostgreSQL/CockroachDB
- Elasticsearch 8.x
- Redis

### Setup
1. Configure database and Elasticsearch in `application.yaml`
2. Run dependencies using Docker Compose:
   ```bash
   docker-compose up -d cockroachdb-us-1 elasticsearch redis-us
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Documentation
- Swagger UI: `/swagger-ui.html` (when application is running)
- API Roadmap: See [API_ROADMAP.md](./API_ROADMAP.md)
- Regional Integration: See [PRODUCT_SERVICE_INTEGRATION_SUMMARY.md](../PRODUCT_SERVICE_INTEGRATION_SUMMARY.md)
