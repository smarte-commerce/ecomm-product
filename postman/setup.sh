#!/bin/bash

# Product Service API - Postman Collection Setup Script
# This script helps validate and set up the Postman collection

set -e

echo "🚀 Product Service API - Postman Collection Setup"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Check if required files exist
check_files() {
    echo "Checking required files..."
    
    if [ -f "Product-Service-API.postman_collection.json" ]; then
        print_status "Collection file found"
    else
        print_error "Collection file not found: Product-Service-API.postman_collection.json"
        exit 1
    fi
    
    if [ -f "Product-Service-Environment.postman_environment.json" ]; then
        print_status "Environment file found"
    else
        print_error "Environment file not found: Product-Service-Environment.postman_environment.json"
        exit 1
    fi
    
    if [ -f "README.md" ]; then
        print_status "Documentation found"
    else
        print_warning "README.md not found"
    fi
}

# Validate JSON files
validate_json() {
    echo -e "\nValidating JSON files..."
    
    # Check if jq is available
    if command -v jq &> /dev/null; then
        if jq empty "Product-Service-API.postman_collection.json" 2>/dev/null; then
            print_status "Collection JSON is valid"
        else
            print_error "Collection JSON is invalid"
            exit 1
        fi
        
        if jq empty "Product-Service-Environment.postman_environment.json" 2>/dev/null; then
            print_status "Environment JSON is valid"
        else
            print_error "Environment JSON is invalid"
            exit 1
        fi
    else
        print_warning "jq not installed - skipping JSON validation"
        print_info "Install jq with: sudo apt-get install jq (Ubuntu) or brew install jq (macOS)"
    fi
}

# Display collection information
show_collection_info() {
    echo -e "\n📋 Collection Information"
    echo "========================"
    
    if command -v jq &> /dev/null; then
        COLLECTION_NAME=$(jq -r '.info.name' "Product-Service-API.postman_collection.json")
        COLLECTION_VERSION=$(jq -r '.info.version' "Product-Service-API.postman_collection.json")
        FOLDER_COUNT=$(jq '.item | length' "Product-Service-API.postman_collection.json")
        REQUEST_COUNT=$(jq '[.item[].item[]] | length' "Product-Service-API.postman_collection.json")
        
        echo "Name: $COLLECTION_NAME"
        echo "Version: $COLLECTION_VERSION"
        echo "Folders: $FOLDER_COUNT"
        echo "Total Requests: $REQUEST_COUNT"
        
        echo -e "\n📁 Folders:"
        jq -r '.item[].name' "Product-Service-API.postman_collection.json" | sed 's/^/  - /'
        
        ENVIRONMENT_NAME=$(jq -r '.name' "Product-Service-Environment.postman_environment.json")
        VARIABLE_COUNT=$(jq '.values | length' "Product-Service-Environment.postman_environment.json")
        
        echo -e "\n🌍 Environment:"
        echo "Name: $ENVIRONMENT_NAME"
        echo "Variables: $VARIABLE_COUNT"
    else
        print_info "Install jq to see detailed collection information"
    fi
}

# Check if Postman is installed
check_postman() {
    echo -e "\n🔍 Checking for Postman..."
    
    if command -v postman &> /dev/null; then
        print_status "Postman CLI found"
        return 0
    fi
    
    # Check for Postman desktop app (macOS)
    if [ -d "/Applications/Postman.app" ]; then
        print_status "Postman desktop app found (macOS)"
        return 0
    fi
    
    # Check for Postman desktop app (Linux)
    if [ -f "/usr/bin/postman" ] || [ -f "/usr/local/bin/postman" ]; then
        print_status "Postman desktop app found (Linux)"
        return 0
    fi
    
    print_warning "Postman not found in PATH"
    print_info "Please install Postman:"
    print_info "  - Desktop: https://www.postman.com/downloads/"
    print_info "  - CLI: npm install -g postman"
}

# Generate sample test script
generate_test_script() {
    echo -e "\n📝 Generating test script..."
    
    cat > "test-api.sh" << 'EOF'
#!/bin/bash

# Simple API test script
# Make sure the Product Service is running before executing this script

BASE_URL="http://localhost:8080"

echo "Testing Product Service API endpoints..."

# Test health endpoint (if available)
echo "1. Testing health endpoint..."
curl -s "$BASE_URL/actuator/health" | head -n 1

# Test public product search
echo -e "\n2. Testing public product search..."
curl -s -X POST "$BASE_URL/api/v1/customer/products/search" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"test","pagination":{"pageNum":0,"pageSize":5}}' | head -n 1

# Test categories endpoint
echo -e "\n3. Testing categories endpoint..."
curl -s "$BASE_URL/api/v1/categories" | head -n 1

# Test brands endpoint
echo -e "\n4. Testing brands endpoint..."
curl -s "$BASE_URL/api/v1/brands" | head -n 1

echo -e "\n✅ Basic tests completed!"
echo "For comprehensive testing, use the Postman collection."
EOF

    chmod +x "test-api.sh"
    print_status "Generated test-api.sh script"
}

# Show setup instructions
show_instructions() {
    echo -e "\n📚 Setup Instructions"
    echo "====================="
    echo "1. Open Postman application"
    echo "2. Click 'Import' button"
    echo "3. Select 'Upload Files' or drag and drop:"
    echo "   - Product-Service-API.postman_collection.json"
    echo "   - Product-Service-Environment.postman_environment.json"
    echo "4. Select the 'Product Service Environment' from the environment dropdown"
    echo "5. Update the baseUrl variable if your API runs on a different port"
    echo "6. Start testing with the Authentication folder"
    echo ""
    echo "📖 For detailed usage instructions, see README.md"
}

# Main execution
main() {
    check_files
    validate_json
    show_collection_info
    check_postman
    generate_test_script
    show_instructions
    
    echo -e "\n🎉 Setup completed successfully!"
    echo "You can now import the collection and environment into Postman."
    
    # Ask if user wants to open the directory
    if command -v open &> /dev/null; then
        echo -e "\nWould you like to open this directory in Finder? (y/N)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            open .
        fi
    elif command -v xdg-open &> /dev/null; then
        echo -e "\nWould you like to open this directory in file manager? (y/N)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            xdg-open .
        fi
    fi
}

# Run main function
main "$@" 
