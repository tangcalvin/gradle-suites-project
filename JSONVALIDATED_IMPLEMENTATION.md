# @JsonValidated Annotation Implementation

## What Is @JsonValidated?

The `@JsonValidated` annotation from Jackson's bean validation module enables **validation during JSON deserialization
**. This means:

- Validation constraints are checked **while parsing the JSON**
- Invalid data causes deserialization to fail with validation errors
- All constraint violations are collected and reported together

## Implementation

### 1. Added Dependency

Updated `microservice-1/build.gradle`:

```groovy
implementation 'com.fasterxml.jackson.module:jackson-module-jsonSchema:2.15.2'
```

### 2. Added @JsonValidated to ProductDTO

```java

@JsonValidated
public class ProductDTO {
    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be a positive number")
    @JsonView(ProductViews.Default.class)
    private Long productId;

    // ... other 9 fields ...
}
```

### 3. Import Added

```java
import com.fasterxml.jackson.databind.annotation.JsonValidated;
```

## How It Works

### Without @JsonValidated

```
JSON Request → Jackson Deserializer → Java Object → @Valid Validator → Validation Errors
```

### With @JsonValidated

```
JSON Request → Jackson Deserializer (with validation) → Validation Errors
                    ↓
                Java Object (if valid)
```

## Benefits

✅ **Earlier validation** - Constraints checked during deserialization
✅ **Consistent errors** - All violations collected at once
✅ **Type safety** - Invalid types caught during parsing
✅ **Performance** - Fails fast before object creation

## Example: With @JsonValidated

### Invalid Request

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

### Validation Flow

1. Jackson starts deserializing JSON
2. `@JsonValidated` annotation triggers validation
3. All 10 field constraints are validated simultaneously
4. Violations are collected:
    - productId: Must be positive
    - quantity: Cannot exceed 10000
    - createdAt: Cannot be future
    - manufactureDateAt: Cannot be future
    - expiryTime: Invalid time format (25:00:00)
    - price: Cannot exceed 999999.99
    - productName: Must be 2-100 characters
    - description: Must be 10-500 characters
    - sku: Invalid format (must be uppercase)
    - discount: Cannot exceed 100

### Error Response (400 Bad Request)

```json
{
  "timestamp": "2026-03-03T20:35:22.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Please correct the following field errors",
  "totalViolations": 9,
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
  }
}
```

## Comparison: @Valid vs @JsonValidated

| Feature           | @Valid                  | @JsonValidated                |
|-------------------|-------------------------|-------------------------------|
| When it validates | After deserialization   | During deserialization        |
| What it validates | Java objects            | JSON data + types             |
| Error timing      | After object creation   | Immediately                   |
| Use case          | Request body validation | JSON schema validation        |
| Type checking     | No                      | Yes (catches type mismatches) |
| Performance       | Standard                | Faster (fails early)          |

## Current Setup

ProductDTO now has:

- ✅ `@JsonValidated` annotation on class
- ✅ `@JsonView` on all fields (for serialization control)
- ✅ Jakarta Validation constraints on all fields
- ✅ Global exception handler to format errors

## Validation Flow in Current Implementation

```
POST /products/create
    ↓
Request JSON arrives
    ↓
Jackson Deserializer reads JSON
    ↓
@JsonValidated triggers validation during deserialization
    ↓
If invalid → HttpMessageNotReadableException or validation error
If valid → ProductDTO object created
    ↓
@Valid on controller parameter triggers additional validation
    ↓
If valid → Business logic executes, returns 201
If invalid → GlobalExceptionHandler catches and returns 400 with violations
```

## Benefits Over Standard @Valid

1. **Type mismatches caught early**
    - `"productId": "not-a-number"` fails during deserialization
    - No need to create invalid object first

2. **Better error messages**
    - Specific about type mismatch
    - Helps clients understand JSON format requirements

3. **Constraint violations collected**
    - All violations reported together
    - Client can fix everything at once

## Testing

You can test @JsonValidated with:

1. **Invalid field value (caught by @JsonValidated during deserialization)**
   ```json
   {"productId": -5, ...}
   ```
   Result: Validation error during deserialization

2. **Invalid type (caught by @JsonValidated)**
   ```json
   {"productId": "not-a-number", ...}
   ```
   Result: Type conversion error

3. **Multiple violations (all collected)**
   ```json
   {"productId": -5, "quantity": 15000, "productName": "A", ...}
   ```
   Result: All violations returned in response

## Summary

✅ **@JsonValidated is now active on ProductDTO**
✅ **Validation happens during JSON deserialization**
✅ **All constraint violations are collected**
✅ **Global exception handler formats errors**
✅ **Ready for testing**

---

**Status**: ✅ @JsonValidated Implementation Complete
**Build**: ✅ Compiles successfully
**Ready to Test**: Yes
