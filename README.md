# Product Microservice

## 📖 Tổng Quan

**Product Microservice** là một dịch vụ vi mô (microservice) toàn diện được xây dựng để quản lý sản phẩm trong hệ thống thương mại điện tử đa nhà cung cấp (multi-vendor). Dịch vụ này hỗ trợ quản lý sản phẩm, danh mục, thương hiệu, kho hàng và tìm kiếm nâng cao với khả năng mở rộng cao.

## 🚀 Tính Năng Chính

### 🛒 Quản Lý Sản Phẩm
- **Quản lý sản phẩm đa dạng**: Hỗ trợ sản phẩm đơn giản và phức tạp với nhiều biến thể
- **Multi-vendor support**: Quản lý sản phẩm của nhiều nhà cung cấp khác nhau
- **Multi-region**: Phân chia dữ liệu theo vùng địa lý
- **Phê duyệt sản phẩm**: Workflow phê duyệt sản phẩm trước khi xuất bản
- **SEO-friendly**: Tự động tạo slug, meta tags cho tối ưu SEO

### 📁 Quản Lý Danh Mục & Thương Hiệu
- **Cấu trúc phân cấp**: Danh mục cha-con không giới hạn cấp độ
- **Thương hiệu**: Quản lý thương hiệu với thông tin chi tiết và xác thực
- **Template tính năng**: Định nghĩa template tính năng cho từng danh mục

### 📦 Quản Lý Kho Hàng
- **Inventory tracking**: Theo dõi số lượng có sẵn, đã bán, đã đặt trước
- **Reservation system**: Hệ thống đặt trước với timeout tự động
- **Optimistic/Pessimistic locking**: Đảm bảo tính nhất quán dữ liệu
- **Multi-location**: Hỗ trợ nhiều kho hàng

### 🔍 Tìm Kiếm Nâng Cao
- **Elasticsearch integration**: Tìm kiếm full-text, filter nâng cao
- **Auto-completion**: Gợi ý tự động khi tìm kiếm
- **Faceted search**: Lọc theo nhiều tiêu chí
- **Analytics**: Theo dõi số liệu tìm kiếm và xem sản phẩm

### 🖼️ Quản Lý Hình Ảnh
- **Multi-format support**: Hỗ trợ nhiều định dạng hình ảnh
- **CDN integration**: Tích hợp AWS S3 và CloudFront
- **Responsive images**: Tự động tạo nhiều kích thước
- **Image optimization**: Tối ưu hóa dung lượng và chất lượng

## 🏗️ Kiến Trúc Hệ Thống

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │   Discovery     │    │   Config Server │
│                 │    │   (Eureka)      │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────▼───────────────────────┐
         │            Product Microservice               │
         │                                               │
         │  ┌─────────────┐  ┌─────────────┐             │
         │  │ Controller  │  │   Service   │             │
         │  │    Layer    │  │    Layer    │             │
         │  └─────────────┘  └─────────────┘             │
         │           │              │                    │
         │  ┌─────────────┐  ┌─────────────┐             │
         │  │ Repository  │  │   Entity    │             │
         │  │    Layer    │  │    Layer    │             │
         │  └─────────────┘  └─────────────┘             │
         └───────────────────────┬───────────────────────┘
                                 │
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │ PostgreSQL  │  │Elasticsearch│  │    Redis    │  │  RabbitMQ   │
    │  Database   │  │   Search    │  │   Cache     │  │  Message    │
    └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
```

## 🛠️ Công Nghệ Sử Dụng

### Backend Framework
- **Spring Boot 3.3.6** - Framework chính
- **Java 21** - Ngôn ngữ lập trình
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - ORM và quản lý database
- **Spring Cloud** - Microservice ecosystem

### Database & Storage
- **PostgreSQL** - Database chính
- **H2** - Database test
- **Elasticsearch 8.17.0** - Tìm kiếm full-text
- **Redis 7.2** - Cache và session storage

### Message Queue & Communication
- **RabbitMQ 3.12** - Message broker
- **OpenFeign** - Service-to-service communication
- **Netflix Eureka** - Service discovery

### Caching & Performance
- **Caffeine** - Local cache
- **Redis** - Distributed cache
- **Connection pooling** - Tối ưu database connection

### External Services
- **AWS S3** - File storage
- **JWT** - Token-based authentication
- **Swagger/OpenAPI** - API documentation

### DevOps & Monitoring
- **Docker & Docker Compose** - Containerization
- **Kibana** - Log visualization
- **Maven** - Build tool

## 📋 Yêu Cầu Hệ Thống

- **Java**: 21+
- **Maven**: 3.6+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **RAM**: Tối thiểu 4GB (khuyến nghị 8GB)
- **Disk**: Tối thiểu 10GB free space

## 🚀 Cài Đặt và Chạy

### 1. Clone Repository
```bash
git clone <repository-url>
cd product-microservice
```

### 2. Khởi Động Dependencies
```bash
# Khởi động tất cả dependencies
docker-compose up -d

# Kiểm tra trạng thái services
docker-compose ps
```

### 3. Cấu Hình Database
```bash
# Tạo database (nếu cần)
# PostgreSQL sẽ tự động tạo database khi khởi động container
```

### 4. Build và Chạy Application
```bash
# Build project
./mvnw clean install

# Chạy application
./mvnw spring-boot:run
```

### 5. Kiểm Tra Services

| Service | URL | Mô tả |
|---------|-----|--------|
| Application | http://localhost:8080 | API chính |
| Elasticsearch | http://localhost:9200 | Search engine |
| Kibana | http://localhost:5601 | Log dashboard |
| RabbitMQ Management | http://localhost:15672 | Message queue UI |

**Credentials mặc định:**
- RabbitMQ: `guest/guest`
- Elasticsearch: `elastic/guest`

## 🔧 Cấu Hình

### Application Configuration
```yaml
# src/main/resources/application.yaml
spring:
  application:
    name: product-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  
  elasticsearch:
    uris: http://localhost:9200
    
  redis:
    host: localhost
    port: 6379
    
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=product_db
DB_USERNAME=postgres
DB_PASSWORD=password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Elasticsearch
ES_HOST=localhost
ES_PORT=9200

# AWS S3
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=ap-southeast-1
S3_BUCKET_NAME=your-bucket-name

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400
```

## 📁 Project Structure

```
src/main/java/com/winnguyen1905/product/
├── common/
│   ├── annotation/           # Custom annotations
│   └── constant/             # Constants and enums
├── config/                   # Configuration classes
├── core/
│   ├── builder/              # Query builders
│   ├── controller/           # REST controllers
│   ├── mapper_v2/            # Entity-DTO mappers
│   ├── model/
│   │   ├── entity/           # Domain entities
│   │   ├── request/          # Request DTOs
│   │   ├── response/         # Response DTOs
│   │   └── viewmodel/        # View models
│   ├── repository/           # Repository interfaces
│   └── service/              # Service interfaces
│       └── impl/             # Service implementations
├── exception/                # Custom exceptions
├── persistance/
│   ├── elasticsearch/        # Elasticsearch entities
│   ├── entity/               # JPA entities
│   └── repository/           # JPA repositories
├── secure/                   # Security configurations
└── util/                     # Utility classes
```

### Service Layer Architecture

The service layer follows a simplified structure:

- **Service Interfaces** (`/service/`): All service interfaces in the main service package
- **Service Implementations** (`/service/impl/`): All implementations in the impl subfolder

Key Services:
- `EnhancedProductService` - Main product management service
- `AdminProductService` - Administrative operations
- `CustomerProductService` - Customer-facing operations
- `InventoryService` - Inventory management
- `VendorProductService` - Vendor-specific operations
- `BrandService` - Brand management
- `VendorCategoryService` - Category management

## 📊 Database Schema

### Entities Chính

#### Products
- **EProduct**: Thông tin sản phẩm cơ bản
- **EProductVariant**: Biến thể sản phẩm (size, color, etc.)
- **EProductImage**: Hình ảnh sản phẩm

#### Categorization
- **ECategory**: Danh mục sản phẩm (cấu trúc phân cấp)
- **EBrand**: Thương hiệu

#### Inventory
- **EInventory**: Thông tin kho hàng
- **Reservation**: Đặt trước sản phẩm

### Relationships
```
EProduct (1) ──── (N) EProductVariant
EProduct (N) ──── (1) ECategory
EProduct (N) ──── (1) EBrand
EProduct (1) ──── (N) EProductImage
EProduct (1) ──── (N) EInventory
EProductVariant (1) ──── (N) EProductImage
```

## 🔌 API Endpoints

### Product Management
```http
GET    /api/v1/products              # Danh sách sản phẩm
GET    /api/v1/products/{id}         # Chi tiết sản phẩm
POST   /api/v1/products              # Tạo sản phẩm mới
PUT    /api/v1/products/{id}         # Cập nhật sản phẩm
DELETE /api/v1/products/{id}         # Xóa sản phẩm
```

### Product Variants
```http
GET    /api/v1/products/{id}/variants          # Danh sách biến thể
POST   /api/v1/products/{id}/variants          # Tạo biến thể mới
PUT    /api/v1/products/{id}/variants/{vid}    # Cập nhật biến thể
DELETE /api/v1/products/{id}/variants/{vid}    # Xóa biến thể
```

### Categories
```http
GET    /api/v1/categories            # Danh sách danh mục
GET    /api/v1/categories/{id}       # Chi tiết danh mục
POST   /api/v1/categories            # Tạo danh mục
PUT    /api/v1/categories/{id}       # Cập nhật danh mục
DELETE /api/v1/categories/{id}       # Xóa danh mục
```

### Search
```http
GET    /api/v1/search/products       # Tìm kiếm sản phẩm
GET    /api/v1/search/suggestions    # Gợi ý tìm kiếm
```

### Inventory
```http
GET    /api/v1/inventories/{id}                # Thông tin tồn kho theo ID
GET    /api/v1/inventories/sku/{sku}           # Thông tin tồn kho theo SKU
GET    /api/v1/inventories/product/{productId} # Danh sách tồn kho theo sản phẩm
PATCH  /api/v1/inventories/{id}/reserve        # Đặt trước sản phẩm
PATCH  /api/v1/inventories/{id}/release        # Hủy đặt trước
POST   /api/v1/inventories/check-availability  # Kiểm tra tồn kho
POST   /api/v1/inventories/reserve-batch       # Đặt trước hàng loạt
```

### Inventory Management with Optimistic Locking

The inventory management system has been enhanced with optimistic locking to ensure data consistency in high-concurrency scenarios. Key features include:

#### Concurrency Control
- **Optimistic locking** using version fields to detect conflicts
- **Automatic retry** for failed operations (up to 3 attempts)
- **Transaction isolation** to maintain data integrity

#### Reservation Workflow
```
┌─────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────┐
│  Start  ├────►│  Reserve    ├────►│  Confirm    ├────►│  End    │
│         │     │  Inventory  │     │  Order      │     │         │
└─────────┘     └─────────────┘     └─────────────┘     └─────────┘
                       │                   │
                       │                   │
                       ▼                   │
                ┌─────────────┐            │
                │  Release    │◄───────────┘
                │  (Cancel)   │
                └─────────────┘
```

#### Redis-Based Reservation System
- Temporary holds stored in Redis
- Configurable expiration times
- Automatic cleanup of expired reservations

#### Batch Operations
- Atomic reservation of multiple items
- All-or-nothing transaction semantics
- Automatic rollback on partial failures

## 🧪 Testing

### Unit Tests
```bash
# Chạy unit tests
./mvnw test

# Test với coverage
./mvnw test jacoco:report
```

### Integration Tests
```bash
# Chạy integration tests
./mvnw test -Dtest="*Integration*"
```

### API Testing
```bash
# Import Postman collection (nếu có)
# Hoặc sử dụng curl để test APIs
curl -X GET http://localhost:8080/api/v1/products
```

## 📈 Monitoring & Logging

### Application Metrics
- **Actuator endpoints**: `/actuator/health`, `/actuator/metrics`
- **Custom metrics**: Product views, search queries, inventory changes

### Logging
- **Logback configuration**: Structured JSON logging
- **Log levels**: Configurable per package
- **Kibana dashboard**: Real-time log analysis

### Health Checks
- Database connectivity
- Elasticsearch cluster health
- Redis connectivity
- External service availability

## 🔒 Security

### Authentication & Authorization
- **JWT-based authentication**
- **Role-based access control** (RBAC)
- **Multi-tenant security** (vendor isolation)

### Data Protection
- **Input validation** với Bean Validation
- **SQL injection prevention** với JPA
- **XSS protection** với Spring Security
- **CORS configuration** cho frontend integration

## 🚀 Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t product-service:latest .

# Run with Docker Compose
docker-compose up -d
```

### Production Configuration
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  product-service:
    image: product-service:latest
    environment:
      SPRING_PROFILES_ACTIVE: production
      DB_HOST: your-production-db
      REDIS_HOST: your-production-redis
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: "0.5"
```

### Kubernetes Deployment
```yaml
# k8s-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
      - name: product-service
        image: product-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
```

## 📚 Documentation

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Architecture Decisions
- **Multi-tenant design**: Phân tách dữ liệu theo vendor
- **Event-driven architecture**: Sử dụng RabbitMQ cho async processing
- **CQRS pattern**: Tách biệt read/write operations cho performance

## 🤝 Contributing

### Development Workflow
1. Fork repository
2. Tạo feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push branch: `git push origin feature/new-feature`
5. Tạo Pull Request

### Code Standards
- **Java Code Style**: Google Java Style Guide
- **Commit Messages**: Conventional Commits
- **Branch Naming**: `feature/`, `bugfix/`, `hotfix/`

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Documentation**: [Wiki](https://github.com/your-repo/wiki)
- **Email**: support@yourcompany.com

---

## 🎯 Roadmap

### Version 2.0
- [ ] GraphQL API support
- [ ] Real-time inventory updates via WebSocket
- [ ] Advanced analytics dashboard
- [ ] Machine learning recommendations

### Version 2.1
- [ ] Multi-language support (i18n)
- [ ] Advanced pricing rules engine
- [ ] Bulk import/export functionality
- [ ] Advanced reporting features

# API Coverage Analysis for Multi-Vendor Ecommerce

## Current API Coverage (Product Management Microservice) - UPDATED AFTER REFACTORING

### ✅ **Excellent Coverage - Core Ecommerce Functionality:**

#### **✅ Product Management (EnhancedProductController):**
- **Complete CRUD Operations** - Create, read, update, delete products
- **Multi-Vendor Support** - Vendor-specific product management
- **Bulk Operations** - Mass updates, imports, status changes
- **Search & Filtering** - Elasticsearch integration with advanced filtering
- **SEO Management** - Slug generation, meta tags, SEO optimization
- **Analytics** - Product performance, view counts, purchase analytics
- **Cache Management** - Redis caching with manual cache control
- **Inventory Integration** - Sync with inventory system

#### **✅ Inventory Management (InventoryController):**
- **Real-time Inventory Tracking** - Available, reserved, sold quantities
- **Reservation System** - Temporary holds with automatic timeout
- **Optimistic/Pessimistic Locking** - Concurrency control
- **Multi-location Support** - Multiple warehouse management
- **Batch Operations** - Bulk inventory updates

#### **✅ Brand & Category Management:**
- **Brand Management (BrandController)** - Complete CRUD with vendor support
- **Category Management (CategoryController)** - Hierarchical structure
- **Multi-vendor Support** - Vendor-specific brands and categories
- **Search Functionality** - Advanced search and filtering

#### **✅ Image Management (ProductImageController):**
- **Multi-format Support** - Various image formats and sizes
- **CDN Integration** - AWS S3 and CloudFront support
- **Responsive Images** - Multiple size variants
- **Bulk Upload** - Mass image operations
- **SEO Optimization** - Alt text, titles, descriptions

#### **✅ Customer Product APIs (ProductController):**
- **Product Discovery** - Browse, search, filter products
- **Product Details** - Complete product information
- **Availability Checking** - Real-time stock validation
- **Inventory Reservation** - Pre-purchase holds

#### **✅ Admin Management (AdminController):**
- **Product Approval** - Admin oversight workflow
- **System Management** - Cache control, monitoring
- **Vendor Performance** - Analytics and reporting
- **Inventory Oversight** - Admin inventory management

### ✅ **Newly Added Controllers - Complete Multi-Vendor Coverage:**

#### **✅ Vendor Management (VendorController - REFACTORED):**
- **Vendor Registration** - Complete onboarding process
- **Profile Management** - Vendor profile and settings
- **Analytics Dashboard** - Sales, performance metrics
- **Order Management** - Vendor order fulfillment
- **Financial Management** - Earnings, payouts, transactions
- **Verification System** - Document upload and verification
- **Settings & Preferences** - Vendor configuration

#### **✅ Order Management (OrderController - NEW):**
- **Order Placement** - Complete checkout process
- **Order Tracking** - Real-time status updates
- **Vendor Fulfillment** - Order processing workflow
- **Return Management** - Return requests and processing
- **Order Analytics** - Performance reporting
- **Admin Oversight** - Order management and dispute resolution

#### **✅ Shopping Cart (CartController - NEW):**
- **Cart Management** - Add, update, remove items
- **Cart Calculations** - Totals, taxes, shipping estimates
- **Coupon System** - Discount application
- **Cart Validation** - Availability and pricing checks
- **Guest Cart Merging** - Login integration
- **Wishlist Integration** - Save for later functionality

#### **✅ Customer Management (CustomerController - NEW):**
- **Profile Management** - Customer information
- **Address Management** - Multiple shipping addresses
- **Wishlist System** - Save favorite products
- **Preferences** - Shopping preferences and settings
- **Notification System** - Customer communications

### ❌ **Still Missing for Complete Ecommerce Platform:**

#### **1. Payment Management Service (Critical)**
```java
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    // Payment processing, refunds, payment methods
    // Multi-vendor commission handling
    // Payout management for vendors
}
```

#### **2. Review & Rating System (Important)**
```java
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    // Product reviews and ratings
    // Vendor reviews and ratings
    // Review moderation and analytics
}
```

#### **3. Shipping & Logistics (Important)**
```java
@RestController
@RequestMapping("/api/v1/shipping")
public class ShippingController {
    // Shipping rate calculation
    // Carrier integration
    // Tracking management
    // Delivery confirmation
}
```

#### **4. Promotion & Discount Management (Important)**
```java
@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {
    // Coupon management
    // Discount rules engine
    // Flash sales and special offers
    // Vendor-specific promotions
}
```

#### **5. Notification System (Important)**
```java
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    // Email notifications
    // SMS notifications
    // Push notifications
    // Notification preferences
}
```

#### **6. User Management & Authentication (Critical)**
```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    // User registration and login
    // JWT token management
    // Password reset
    // Social media authentication
}
```

#### **7. Support & Help Desk (Nice to Have)**
```java
@RestController
@RequestMapping("/api/v1/support")
public class SupportController {
    // Support tickets
    // Live chat integration
    // FAQ management
    // Knowledge base
}
```

### **API Architecture Summary:**

#### **✅ Current Complete Coverage (~80% of Ecommerce Functionality):**
1. ✅ **Product Management** - Complete with variants, images, SEO
2. ✅ **Inventory Management** - Real-time tracking with reservations
3. ✅ **Vendor Management** - Registration, analytics, order management
4. ✅ **Order Management** - Full order lifecycle
5. ✅ **Shopping Cart** - Complete cart functionality
6. ✅ **Customer Management** - Profile, wishlist, preferences
7. ✅ **Brand & Category** - Complete hierarchical management
8. ✅ **Admin Management** - System oversight and analytics

#### **❌ Missing Critical Services (~20% for Production):**
1. ❌ **Payment Processing** - Payment gateway integration
2. ❌ **Review System** - Customer feedback and ratings
3. ❌ **Shipping Management** - Logistics and tracking
4. ❌ **Promotion Engine** - Discounts and coupons
5. ❌ **Notification System** - Customer communications
6. ❌ **User Authentication** - Registration and login

### **Recommendation:**
The current implementation is excellent for MVP and early development. For production deployment, prioritize implementing Payment, Review, and Shipping services to complete the platform.

**Total API Coverage: ~80% of a complete multi-vendor ecommerce platform**
