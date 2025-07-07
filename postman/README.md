# Product Service API - Postman Collection

This directory contains comprehensive Postman collections for testing the Product Service API endpoints.

## Files

- **`Product-Service-API.postman_collection.json`** - Main collection with all API endpoints
- **`Product-Service-Environment.postman_environment.json`** - Environment variables for different environments
- **`README.md`** - This documentation file

## Quick Start

1. **Import Collections**
   - Open Postman
   - Click "Import" → "Upload Files"
   - Select both JSON files
   - Import the collection and environment

2. **Set Environment**
   - Select "Product Service Environment" from the environment dropdown
   - Update the `baseUrl` variable if needed (default: `http://localhost:8080`)

3. **Authentication**
   - Use the "Login" request in the "Authentication" folder
   - Copy the returned JWT token
   - Set it in the `authToken` environment variable

## Collection Structure

### 1. Authentication
- **Login** - Authenticate and get JWT token

### 2. Product CRUD
- **Create Product** - Create new product with variants
- **Get Product** - Retrieve product by ID
- **Update Product** - Update product information
- **Delete Product** - Soft delete product

### 3. Product Search
- **Search Products** - Advanced search with filters and sorting
- **Get Popular Products** - Get trending products
- **Get Related Products** - Get products related to specific product

### 4. Categories
- **Get All Categories** - List all categories
- **Create Category** - Create new category
- **Update Category** - Update category information
- **Get Category Tree** - Get hierarchical category structure

### 5. Brands
- **Get All Brands** - List all brands
- **Create Brand** - Create new brand
- **Update Brand** - Update brand information

### 6. Customer Products
- **Search Products (Customer)** - Public product search
- **Get Product Detail** - Get detailed product information
- **Check Product Availability** - Check inventory availability
- **Reserve Inventory** - Reserve products for purchase

### 7. Inventory Management
- **Get Product Inventories** - Get inventory for product
- **Update Inventory** - Update inventory levels
- **Reserve Inventory** - Reserve inventory quantities

### 8. Product Images
- **Get Product Images** - Get all images for product
- **Upload Product Image** - Upload new product image
- **Update Image Metadata** - Update image information

### 9. Vendor Management
- **Get Vendor Products** - Get products for specific vendor
- **Register Vendor** - Register new vendor
- **Get Vendor Dashboard** - Get vendor analytics

### 10. Bulk Operations
- **Bulk Update Product Status** - Update multiple products at once
- **Bulk Import Products** - Import multiple products
- **Bulk Delete Products** - Delete multiple products

### 11. Admin Operations
- **Get All Inventories** - Admin view of all inventories
- **Get Pending Approval Products** - Products awaiting approval
- **Approve Product** - Approve/reject product

### 12. Elasticsearch
- **Search Products (Elasticsearch)** - Direct Elasticsearch search
- **Sync Product** - Sync product to Elasticsearch
- **Full Reindex** - Reindex all products

## Environment Variables

### Base Configuration
- `baseUrl` - API base URL (default: `http://localhost:8080`)
- `authToken` - JWT token for authentication
- `adminToken` - Admin JWT token
- `vendorToken` - Vendor JWT token
- `customerToken` - Customer JWT token

### Test Data IDs
- `vendorId` - Test vendor ID
- `productId` - Test product ID
- `categoryId` - Test category ID
- `brandId` - Test brand ID
- `shopId` - Test shop ID
- `inventoryId` - Test inventory ID
- `variantId` - Test variant ID
- `imageId` - Test image ID

### User Credentials
- `testEmail` - Test user email
- `adminEmail` - Admin user email
- `vendorEmail` - Vendor user email
- `customerEmail` - Customer user email
- `defaultPassword` - Default password for test users

### Configuration
- `regionPartition` - Region (default: "US")
- `currency` - Currency (default: "USD")
- `locale` - Locale (default: "en_US")
- `timezone` - Timezone (default: "America/New_York")
- `pageSize` - Default page size (default: 20)
- `pageNumber` - Default page number (default: 0)

## Authentication & Authorization

### User Roles
- **ADMIN** - Full access to all endpoints
- **VENDOR** - Access to vendor-specific endpoints
- **CUSTOMER** - Access to customer-facing endpoints

### Authentication Flow
1. Use the "Login" request with appropriate credentials
2. Copy the JWT token from the response
3. Set it in the `authToken` environment variable
4. The collection is configured to use Bearer token authentication

### Role-based Access
- Admin endpoints require `ADMIN` role
- Vendor endpoints require `VENDOR` or `ADMIN` role
- Customer endpoints are mostly public or require `CUSTOMER` role

## Request Examples

### Create Product
```json
{
  "name": "Sample Product",
  "description": "This is a sample product",
  "productType": "SIMPLE",
  "vendorId": "{{vendorId}}",
  "shopId": "{{shopId}}",
  "region": "US",
  "basePrice": 29.99,
  "variants": [
    {
      "sku": "SAMPLE-001",
      "name": "Default Variant",
      "price": 29.99,
      "inventoryQuantity": 100
    }
  ]
}
```

### Search Products
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

### Reserve Inventory
```json
{
  "reservationId": "{{$randomUUID}}",
  "items": [
    {
      "productId": "{{productId}}",
      "variantId": "{{variantId}}",
      "quantity": 2
    }
  ],
  "expiresAt": "2024-12-31T23:59:59Z"
}
```

## Error Handling

### Common HTTP Status Codes
- `200 OK` - Success
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate SKU)
- `422 Unprocessable Entity` - Validation errors
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      "Product name is required",
      "Price must be greater than 0"
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## Testing Tips

### 1. Sequential Testing
- Start with authentication
- Create categories and brands first
- Create products referencing categories/brands
- Test search and filtering

### 2. Data Dependencies
- Some endpoints require existing data
- Use the environment variables for consistent test data
- Create test data in the correct order

### 3. File Uploads
- For image upload endpoints, select actual image files
- Supported formats: JPG, PNG, WEBP
- Maximum file size: 10MB

### 4. Pagination
- Use `page` and `size` query parameters
- Default page size is 20
- Page numbers start from 0

### 5. UUID Generation
- Use `{{$randomUUID}}` for generating test UUIDs
- Environment variables contain pre-defined test UUIDs

## Environment Setup

### Local Development
```
baseUrl: http://localhost:8080
```

### Staging Environment
```
baseUrl: https://staging-api.example.com
```

### Production Environment
```
baseUrl: https://api.example.com
```

## Additional Resources

### API Documentation
- OpenAPI/Swagger documentation available at `/swagger-ui.html`
- API specs available at `/v3/api-docs`

### Monitoring
- Health check: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`

### Support
- For API issues, check the application logs
- For collection issues, verify environment variables
- Ensure proper authentication tokens are set

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check if `authToken` is set correctly
   - Verify token hasn't expired
   - Ensure proper role permissions

2. **404 Not Found**
   - Verify the endpoint URL
   - Check if resource exists
   - Ensure correct HTTP method

3. **400 Bad Request**
   - Validate request body format
   - Check required fields
   - Verify data types

4. **File Upload Issues**
   - Ensure file is selected in form-data
   - Check file size limits
   - Verify supported file formats

### Debug Steps
1. Check environment variables are set
2. Verify authentication token
3. Review request body format
4. Check server logs for detailed errors
5. Use Postman console for debugging

## Contributing

To add new endpoints:
1. Create new request in appropriate folder
2. Add environment variables if needed
3. Update this README with documentation
4. Test thoroughly before committing

---

**Note**: This collection is designed for development and testing purposes. Always use appropriate credentials and never commit sensitive data to version control. 
