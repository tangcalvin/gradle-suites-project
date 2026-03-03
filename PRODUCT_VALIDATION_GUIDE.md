# ProductDTO Validation Guide

This guide demonstrates how to use the new `ProductController` and `ProductDTO` with comprehensive constraint violation
collection using `@Validated` annotation.

## Overview

The `ProductDTO` contains 10 fields with different types:

1. **productId** (Long) - Positive number
2. **quantity** (Integer) - Range 1-10000
3. **createdAt** (OffsetDateTime) - Past or present date/time
4. **manufactureDateAt** (LocalDate) - Past or present date
5. **expiryTime** (LocalTime) - Time value
6. **price** (BigDecimal) - Decimal number 0.01 to 999999.99
7. **productName** (String) - 2-100 characters
8. **description** (String) - 10-500 characters
9. **sku** (String) - 5-15 uppercase alphanumeric characters
10. **discount** (BigDecimal) - Decimal 0.00 to 100.00

## Endpoints

### POST /products/create

Creates a product with validation. Returns all constraint violations if validation fails.

### POST /products/create-detailed

Alternative endpoint with the same validation logic.

### GET /products/{productId}

Retrieves a product by ID (for testing).

## Example JSON Payloads

### Valid Payload (All Fields Correct)

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

### Invalid Payload (All Fields Invalid - Collect All Violations)

```json
{
  "productId": -5,
  "quantity": 15000,
  "createdAt": "2030-03-03T10:30:00+00:00",
  "manufactureDateAt": "2030-12-31",
  "expiryTime": "not-a-time",
  "price": 10000000.00,
  "productName": "A",
  "description": "short",
  "sku": "abc12",
  "discount": 150.00
}
```

**Expected Response:**

```json
{
  "timestamp": "2026-03-03T10:35:22.123",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "totalViolations": 10,
  "violations": {
    "productId": [
      "Product ID must be a positive number"
    ],
    "quantity": [
      "Quantity cannot exceed 10000"
    ],
    "createdAt": [
      "Created timestamp must be in the past or present"
    ],
    "manufactureDateAt": [
      "Manufacture date must be in the past or present"
    ],
    "expiryTime": [
      "ExpiryTime is required"
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

### Partial Invalid Payload (Some Fields Invalid)

```json
{
  "productId": 1,
  "quantity": 15000,
  "createdAt": "2026-03-03T10:30:00+00:00",
  "manufactureDateAt": "2025-01-01",
  "expiryTime": "18:30:00",
  "price": 29.99,
  "productName": "A",
  "description": "short",
  "sku": "WIDG12345",
  "discount": 15.50
}
```

**Expected Response (Only 3 violations):**

```json
{
  "timestamp": "2026-03-03T10:36:45.456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "totalViolations": 3,
  "violations": {
    "quantity": [
      "Quantity cannot exceed 10000"
    ],
    "productName": [
      "Product name must be between 2 and 100 characters"
    ],
    "description": [
      "Description must be between 10 and 500 characters"
    ]
  }
}
```

### Type Mismatch Example (Invalid Data Types)

```json
{
  "productId": "not-a-number",
  "quantity": "one-hundred",
  "createdAt": "invalid-date",
  "manufactureDateAt": "invalid-date",
  "expiryTime": "invalid-time",
  "price": "not-a-price",
  "productName": "Valid Product",
  "description": "This is a valid description with enough characters",
  "sku": "VALID12345",
  "discount": "not-a-discount"
}
```

**Expected Response (Type conversion errors from Spring):**
Spring will return a 400 error before reaching validator if data types are incompatible.

## Testing with cURL

### Test Valid Payload

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
  }'
```

### Test Invalid Payload (Collect All Violations)

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
  }'
```

## Global Exception Handler

The `GlobalExceptionHandler` class intercepts `MethodArgumentNotValidException` and collects all constraint violations
in a structured format:

- **timestamp**: When the error occurred
- **status**: HTTP status code (400)
- **error**: Error type ("Validation Failed")
- **message**: General error message
- **violations**: Map of field names to list of constraint violation messages
- **totalViolations**: Count of all constraint violations across all fields

This allows clients to collect and display all validation errors at once, rather than failing on the first error.

## Key Features

1. **Multi-field Validation**: All 10 fields are validated simultaneously
2. **Detailed Error Messages**: Each field has specific, descriptive validation messages
3. **Type Variety**: Demonstrates validation of Long, Integer, OffsetDateTime, LocalDate, LocalTime, BigDecimal, and
   String types
4. **Structured Response**: Returns all violations in a well-organized JSON structure
5. **Global Exception Handling**: Centralized error handling via GlobalExceptionHandler

## Validation Constraints Used

- `@NotNull` - Ensures required fields are not null
- `@NotBlank` - Ensures string fields are not blank
- `@Positive` - Ensures positive numeric values
- `@Min` / `@Max` - Range validation for integers
- `@DecimalMin` / `@DecimalMax` - Range validation for decimals
- `@Digits` - Precision validation for decimals
- `@Size` - String length validation
- `@Pattern` - Regular expression validation for SKU format
- `@PastOrPresent` - Date/time validation

