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
GET    /api/v1/inventory/{sku}       # Thông tin tồn kho
POST   /api/v1/inventory/reserve     # Đặt trước sản phẩm
POST   /api/v1/inventory/confirm     # Xác nhận bán hàng
POST   /api/v1/inventory/release     # Hủy đặt trước
```

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

---

**Made with ❤️ by the Product Team**
