# Quick Reference: Testing ProductController

## Direct Terminal Testing with cURL

### Test 1: Valid Product (Should Return 201)

```bash
curl -X POST http://localhost:8080/products/create \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 100,
    "createdAt": "2026-03-03T10:30:00+00:00",
    "manufactureDateAt": "2025-01-01",
    "expiryTime": "18:30:00",
    "price": 29.99,
    "productName": "Premium Widget",
    "description": "High-quality widget with advanced features and excellent durability",
    "sku": "WIDG12345",
    "discount": 15.50
  }' | jq .
```

### Test 2: All Fields Invalid (Should Return 400 with All 9 Violations)

```bash
curl -X POST http://localhost:8080/products/create \
  -H "Content-Type: application/json" \
  -d '{
    "productId": -5,
    "quantity": 15000,
    "createdAt": "2030-03-03T10:30:00+00:00",
    "manufactureDateAt": "2030-12-31",
    "expiryTime": "25:00:00",
    "price": 10000000.00,
    "productName": "A",
    "description": "short",
    "sku": "abc12",
    "discount": 150.00
  }' | jq .
```

### Test 3: Single Violation (Quantity Too High)

```bash
curl -X POST http://localhost:8080/products/create \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 15000,
    "createdAt": "2026-03-03T10:30:00+00:00",
    "manufactureDateAt": "2025-01-01",
    "expiryTime": "18:30:00",
    "price": 29.99,
    "productName": "Premium Widget",
    "description": "High-quality widget with advanced features and excellent durability",
    "sku": "WIDG12345",
    "discount": 15.50
  }' | jq .
```

### Test 4: GET Endpoint (Retrieve Product)

```bash
curl -X GET http://localhost:8080/products/1 | jq .
```

---

## Postman Configuration

### Setup

1. Create new collection: `ProductController Tests`
2. Add new POST request: `Create Product`
3. URL: `http://localhost:8080/products/create`
4. Headers:
    - Key: `Content-Type`
    - Value: `application/json`

### Body Examples

**Tab 1 - Valid Product:**

```json
{
  "productId": 1,
  "quantity": 100,
  "createdAt": "2026-03-03T10:30:00+00:00",
  "manufactureDateAt": "2025-01-01",
  "expiryTime": "18:30:00",
  "price": 29.99,
  "productName": "Premium Widget",
  "description": "High-quality widget with advanced features and excellent durability",
  "sku": "WIDG12345",
  "discount": 15.50
}
```

**Tab 2 - All Invalid:**

```json
{
  "productId": -5,
  "quantity": 15000,
  "createdAt": "2030-03-03T10:30:00+00:00",
  "manufactureDateAt": "2030-12-31",
  "expiryTime": "25:00:00",
  "price": 10000000.00,
  "productName": "A",
  "description": "short",
  "sku": "abc12",
  "discount": 150.00
}
```

---

## Expected HTTP Status Codes

| Scenario        | Status | Description                         |
|-----------------|--------|-------------------------------------|
| Valid product   | 201    | Created successfully                |
| Invalid product | 400    | Validation failed, check violations |
| Server error    | 500    | Unexpected error                    |

---

## Response Format

### Success Response (201 Created)

```json
{
  "productId": 1,
  "quantity": 100,
  "createdAt": "2026-03-03T10:30:00+00:00",
  "manufactureDateAt": "2025-01-01",
  "expiryTime": "18:30:00",
  "price": 29.99,
  "productName": "Premium Widget",
  "description": "High-quality widget with advanced features and excellent durability",
  "sku": "WIDG12345",
  "discount": 15.50
}
```

### Error Response (400 Bad Request)

```json
{
  "timestamp": "2026-03-03T20:28:15.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "totalViolations": 9,
  "violations": {
    "productId": ["Product ID must be a positive number"],
    "quantity": ["Quantity cannot exceed 10000"],
    "createdAt": ["Created timestamp must be in the past or present"],
    "manufactureDateAt": ["Manufacture date must be in the past or present"],
    "price": ["Price cannot exceed 999999.99"],
    "productName": ["Product name must be between 2 and 100 characters"],
    "description": ["Description must be between 10 and 500 characters"],
    "sku": ["SKU must be between 5 and 15 uppercase alphanumeric characters"],
    "discount": ["Discount cannot exceed 100"]
  }
}
```

---

## Files Created

### Code Files

- ✅ `microservice-1/src/main/java/com/philomath/dto/ProductDTO.java` - DTO with 10 fields
- ✅ `microservice-1/src/main/java/com/philomath/controller/ProductController.java` - REST endpoints
- ✅ `microservice-1/src/main/java/com/philomath/controller/GlobalExceptionHandler.java` - Enhanced exception handler

### Documentation Files

- 📄 `POSTMAN_PRODUCT_SAMPLES.md` - 10 sample payloads with responses
- 📄 `PRODUCT_VALIDATION_GUIDE.md` - Comprehensive validation guide
- 📄 `BEAN_CONFLICT_FIX.md` - Technical explanation of bean conflict fix
- 📄 `SETUP_COMPLETE.md` - Complete setup summary
- 📄 `QUICK_REFERENCE.md` - This file

---

## Build & Run

### Build

```bash
cd /Users/tangcalvin/Development/java/gradle-suites-project
./gradlew clean build -x test
```

### Run

```bash
./gradlew :microservice-1:bootRun
```

### Access Swagger/OpenAPI UI

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

---

## Troubleshooting

### Port Already in Use

```bash
lsof -i :8080
kill -9 <PID>
```

### Force Rebuild

```bash
./gradlew clean :microservice-1:build -x test
```

### See Build Details

```bash
./gradlew build --stacktrace
```

---

## Key Points to Remember

✅ All 10 fields validated together - no early exit on first error
✅ Response includes `totalViolations` count
✅ Violations grouped by field name
✅ Each field can have multiple violations (returned as a list)
✅ DateTime formats must be ISO 8601 (see POSTMAN_PRODUCT_SAMPLES.md)
✅ SKU must be uppercase alphanumeric, 5-15 characters
✅ Dates cannot be in the future (@PastOrPresent)

---

## Contact & Notes

Ready to test! All files are in place and the application should start without bean conflicts.

For detailed examples, see: `POSTMAN_PRODUCT_SAMPLES.md`
For validation details, see: `PRODUCT_VALIDATION_GUIDE.md`
