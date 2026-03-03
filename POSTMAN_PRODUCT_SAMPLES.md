# Postman JSON Samples for ProductController

These are ready-to-use JSON payloads for testing the ProductController endpoints in Postman.

## Endpoint URLs

- **POST (Create)**: `http://localhost:8080/products/create`
- **POST (Create Detailed)**: `http://localhost:8080/products/create-detailed`
- **GET**: `http://localhost:8080/products/1`

---

## ✅ Sample 1: Valid Product (All Fields Correct)

Use this to test a successful product creation (HTTP 201).

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

**Expected Response (201 Created):**

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

---

## ❌ Sample 2: All Fields Invalid (Collect All 10 Violations)

Use this to test collecting all constraint violations at once.

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

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:35:22.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
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
  },
  "totalViolations": 9
}
```

---

## ❌ Sample 3: Partial Invalid - Quantity Too High

Use this to test single field violation.

```json
{
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
}
```

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:36:45.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "quantity": [
      "Quantity cannot exceed 10000"
    ]
  },
  "totalViolations": 1
}
```

---

## ❌ Sample 4: Multiple Violations - String Fields Too Short

Use this to test multiple field violations.

```json
{
  "productId": 1,
  "quantity": 100,
  "createdAt": "2026-03-03T10:30:00+00:00",
  "manufactureDateAt": "2025-01-01",
  "expiryTime": "18:30:00",
  "price": 29.99,
  "productName": "A",
  "description": "short",
  "sku": "abc12",
  "discount": 15.50
}
```

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:37:10.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "productName": [
      "Product name must be between 2 and 100 characters"
    ],
    "description": [
      "Description must be between 10 and 500 characters"
    ],
    "sku": [
      "SKU must be between 5 and 15 uppercase alphanumeric characters"
    ]
  },
  "totalViolations": 3
}
```

---

## ❌ Sample 5: Future Dates (CreatedAt in Future)

Use this to test PastOrPresent validation.

```json
{
  "productId": 1,
  "quantity": 100,
  "createdAt": "2030-03-03T10:30:00+00:00",
  "manufactureDateAt": "2030-12-31",
  "expiryTime": "18:30:00",
  "price": 29.99,
  "productName": "Premium Widget",
  "description": "High-quality widget with advanced features and excellent durability",
  "sku": "WIDG12345",
  "discount": 15.50
}
```

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:38:00.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "createdAt": [
      "Created timestamp must be in the past or present"
    ],
    "manufactureDateAt": [
      "Manufacture date must be in the past or present"
    ]
  },
  "totalViolations": 2
}
```

---

## ❌ Sample 6: Price Exceeds Maximum

Use this to test DecimalMax validation.

```json
{
  "productId": 1,
  "quantity": 100,
  "createdAt": "2026-03-03T10:30:00+00:00",
  "manufactureDateAt": "2025-01-01",
  "expiryTime": "18:30:00",
  "price": 1000000.00,
  "productName": "Premium Widget",
  "description": "High-quality widget with advanced features and excellent durability",
  "sku": "WIDG12345",
  "discount": 15.50
}
```

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:39:15.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "price": [
      "Price cannot exceed 999999.99"
    ]
  },
  "totalViolations": 1
}
```

---

## ❌ Sample 7: Discount Exceeds 100

Use this to test DecimalMax for discount.

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
  "discount": 150.00
}
```

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:40:00.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "discount": [
      "Discount cannot exceed 100"
    ]
  },
  "totalViolations": 1
}
```

---

## ❌ Sample 8: Invalid SKU Format (Lowercase Letters)

Use this to test Pattern validation for SKU.

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
  "sku": "widg12345",
  "discount": 15.50
}
```

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:41:00.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "sku": [
      "SKU must be between 5 and 15 uppercase alphanumeric characters"
    ]
  },
  "totalViolations": 1
}
```

---

## ❌ Sample 9: Negative ProductId

Use this to test @Positive validation.

```json
{
  "productId": -100,
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

**Expected Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T10:42:00.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "violations": {
    "productId": [
      "Product ID must be a positive number"
    ]
  },
  "totalViolations": 1
}
```

---

## ✅ Sample 10: Valid with Different Values

Alternative valid product with different values.

```json
{
  "productId": 999,
  "quantity": 50,
  "createdAt": "2026-01-15T14:45:30+00:00",
  "manufactureDateAt": "2024-06-20",
  "expiryTime": "12:00:00",
  "price": 149.99,
  "productName": "Deluxe Gadget Pro",
  "description": "Professional-grade gadget designed for advanced users with premium build quality",
  "sku": "GADGET001",
  "discount": 5.00
}
```

**Expected Response (201 Created):**

```json
{
  "productId": 999,
  "quantity": 50,
  "createdAt": "2026-01-15T14:45:30+00:00",
  "manufactureDateAt": "2024-06-20",
  "expiryTime": "12:00:00",
  "price": 149.99,
  "productName": "Deluxe Gadget Pro",
  "description": "Professional-grade gadget designed for advanced users with premium build quality",
  "sku": "GADGET001",
  "discount": 5.00
}
```

---

## 🔧 How to Use in Postman

1. **Create a new POST request**
    - URL: `http://localhost:8080/products/create`
    - Method: `POST`

2. **Set Headers**
    - Key: `Content-Type`
    - Value: `application/json`

3. **Copy JSON into Body**
    - Select `raw` option
    - Select `JSON` from dropdown
    - Paste one of the samples above

4. **Click Send**

5. **Observe the Response**
    - Success (201): Product is created
    - Error (400): See violations in response

---

## ⏰ DateTime Format Notes

- **OffsetDateTime (createdAt)**: `2026-03-03T10:30:00+00:00` (ISO 8601 with timezone)
- **LocalDate (manufactureDateAt)**: `2025-01-01` (YYYY-MM-DD)
- **LocalTime (expiryTime)**: `18:30:00` (HH:MM:SS)

---

## 💡 Tips for Testing

- Use Sample 1 first to confirm your setup works
- Use Sample 2 to see how multiple violations are collected
- Use Samples 3-9 to test specific constraint types
- Check the violation messages to understand what validations failed
- All violations are returned in a single response, not one at a time

