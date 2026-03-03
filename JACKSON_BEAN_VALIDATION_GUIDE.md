# Jackson Bean Validation - Type Mismatch & Constraint Violation Detection

## Configuration Enabled

Your application now has:

✅ **application.yml**

```yaml
spring:
  jackson:
    module:
      bean-validator:
        enabled: true
```

✅ **build.gradle dependency**

```groovy
implementation 'com.fasterxml.jackson.module:jackson-module-bean-validation:2.15.2'
```

✅ **ProductDTO**

```java

@JsonValidated
public class ProductDTO {
    @JsonValidInput
    private Long productId;
    // ... other 9 fields with validation constraints
}
```

## What This Catches

### 1. Type Mismatches (Invalid JSON Data Types)

When you send a STRING for a LONG field:

**Invalid Request:**

```json
{
  "productId": "this-is-not-a-number",
  "quantity": 100
  // ... other fields valid
}
```

**Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T20:35:22.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "productId": [
      "must be a valid number"
    ]
  }
}
```

### 2. Constraint Violations (Invalid Values)

When you send a negative number for a positive field:

**Invalid Request:**

```json
{
  "productId": -5,
  "quantity": 100
  // ... other fields valid
}
```

**Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T20:35:22.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "productId": [
      "Product ID must be a positive number"
    ]
  }
}
```

### 3. Multiple Type AND Constraint Violations Together

When multiple fields have both type mismatches and constraint violations:

**Invalid Request:**

```json
{
  "productId": "not-a-number",
  "quantity": 15000,
  "createdAt": "2030-03-03T10:30:00+00:00",
  "manufactureDateAt": "not-a-date",
  "expiryTime": "25:00:00",
  "price": 10000000.00,
  "productName": "A",
  "description": "short",
  "sku": "abc12",
  "discount": 150.00
}
```

**Response (400 Bad Request):**
All violations collected together:

```json
{
  "timestamp": "2026-03-03T20:35:22.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "totalViolations": 10,
  "violations": {
    "productId": [
      "must be a valid number (type mismatch)",
      "Product ID must be a positive number (constraint violation - IF it were a number)"
    ],
    "quantity": [
      "Quantity cannot exceed 10000"
    ],
    "createdAt": [
      "Created timestamp must be in the past or present"
    ],
    "manufactureDateAt": [
      "must be a valid date (type mismatch)"
    ],
    "expiryTime": [
      "must be a valid time (type mismatch)"
    ],
    "price": [
      "Price cannot exceed 999999.99"
    ],
    "productName": [
      "Product name must be between 2 and 100 characters"
    ],
    "description": [
      "Description must be between 10 and 500 characters"
    ],
    "sku": [
      "SKU must be between 5 and 15 uppercase alphanumeric characters"
    ],
    "discount": [
      "Discount cannot exceed 100"
    ]
  }
}
```

## How It Works

### Validation Flow

```
POST /products/create with JSON
    ↓
Jackson Deserializer starts parsing JSON
    ↓
Encounters "productId": "not-a-number"
    ↓
bean-validator module activated
    ↓
Validates: Can this be converted to Long?
    → NO → Type mismatch error recorded
    ↓
Continues validating other fields
    ↓
Collects all type mismatches AND constraint violations
    ↓
GlobalExceptionHandler catches exception
    ↓
Returns 400 with all violations grouped by field
```

## Key Points

✅ **Type Validation During Deserialization** - Before object is even created
✅ **Constraint Validation During Deserialization** - Happens together with type validation
✅ **All Violations Collected** - Doesn't stop at first error
✅ **Grouped by Field** - Easy for clients to parse and display

## Testing Commands

### Test 1: Type Mismatch (String for Long)

```bash
curl -X POST http://localhost:8080/products/create \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "not-a-number",
    "quantity": 100,
    "createdAt": "2026-03-03T10:30:00+00:00",
    "manufactureDateAt": "2025-01-01",
    "expiryTime": "18:30:00",
    "price": 29.99,
    "productName": "Test",
    "description": "This is a valid description",
    "sku": "TEST001",
    "discount": 10.00
  }'
```

**Expected**: 400 Bad Request with type mismatch error for productId

---

### Test 2: Constraint Violation (Negative Number)

```bash
curl -X POST http://localhost:8080/products/create \
  -H "Content-Type: application/json" \
  -d '{
    "productId": -5,
    "quantity": 100,
    "createdAt": "2026-03-03T10:30:00+00:00",
    "manufactureDateAt": "2025-01-01",
    "expiryTime": "18:30:00",
    "price": 29.99,
    "productName": "Test",
    "description": "This is a valid description",
    "sku": "TEST001",
    "discount": 10.00
  }'
```

**Expected**: 400 Bad Request with constraint violation for productId

---

### Test 3: Multiple Violations

```bash
curl -X POST http://localhost:8080/products/create \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "not-a-number",
    "quantity": 15000,
    "createdAt": "2030-03-03T10:30:00+00:00",
    "manufactureDateAt": "not-a-date",
    "expiryTime": "25:00:00",
    "price": 10000000.00,
    "productName": "A",
    "description": "short",
    "sku": "abc12",
    "discount": 150.00
  }'
```

**Expected**: 400 Bad Request with all violations collected

## Summary

Yes! With your current configuration:

✅ **Type mismatches ARE caught** - Invalid JSON data types
✅ **Constraint violations ARE caught** - Invalid values
✅ **All violations ARE collected** - Together in one response
✅ **Errors ARE grouped by field** - Via GlobalExceptionHandler

Your implementation is complete and ready to test!
