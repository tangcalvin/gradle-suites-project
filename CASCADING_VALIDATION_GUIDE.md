# Cascading Validation with @Valid - Complete Guide

## Overview

I've created a nested **Address** object that demonstrates cascading validation. When the User object is validated, the
nested Address object is automatically validated as well through the `@Valid` annotation.

## What is Cascading Validation?

**Cascading validation** means that when you validate a parent object, the validation also applies to its child objects
recursively. This is enabled by the `@Valid` annotation on the nested object field.

## Implementation

### 1. Address DTO (New Child Object)

**Location:** `microservice-1/src/main/java/com/philomath/dto/Address.java`

```java
public class Address {
    @NotBlank(message = "Street cannot be blank")
    @Size(min = 5, max = 100, message = "Street must be between 5 and 100 characters")
    private String street;

    @NotBlank(message = "City cannot be blank")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;

    @NotBlank(message = "State/Province cannot be blank")
    @Size(min = 2, max = 50, message = "State/Province must be between 2 and 50 characters")
    private String state;

    @NotBlank(message = "Postal code cannot be blank")
    @Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Postal code must be 3-10 alphanumeric characters (uppercase)")
    private String postalCode;

    @NotBlank(message = "Country cannot be blank")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    private String country;
}
```

**Postal Code Format Examples:**

- `12345` (US style - 5 digits)
- `90210` (US style)
- `M5H2N2` (Canadian style)
- `SW1A1AA` (UK style)
- `75001` (French style)

### 2. User DTO (Modified with Address)

**Location:** `microservice-1/src/main/java/com/philomath/dto/User.java`

```java

@Valid  // ← KEY: Enables cascading validation
@NotNull(message = "Address cannot be null", groups = Views.ValidationGroups.Endpoint1Validation.class)
@JsonView(Views.Endpoint1.class)
private Address address;  // Changed from String to Address object
```

**Key Change:** `address` field changed from `String` to `Address` object

## How Cascading Validation Works

### Without @Valid (No Cascading)

```java
private Address address;  // ❌ Only validates that address is not null
// ❌ Doesn't validate Address fields (street, city, etc.)
```

### With @Valid (Cascading)

```java

@Valid  // ✅ Validates the Address object AND all its fields
private Address address;
```

### Validation Flow Diagram

```
POST /users/endpoint1
        ↓
    JSON Request
   {
     "id": 1,
     "username": "john.doe",
     "address": {
       "street": "123 Main St",
       "city": "Springfield",
       "state": "IL",
       "postalCode": "62701",
       "country": "USA"
     }
   }
        ↓
@Validated(Endpoint1Validation.class) activated
        ↓
Validate User object
  ├─ id: @NotNull, @Positive ✓
  ├─ username: @NotBlank, @Size ✓
  ├─ email: @NotBlank, @Email ✓
  ├─ phone: @NotBlank, @Pattern ✓
  └─ @Valid address (CASCADE INTO Address OBJECT)
       ├─ street: @NotBlank, @Size ✓
       ├─ city: @NotBlank, @Size ✓
       ├─ state: @NotBlank, @Size ✓
       ├─ postalCode: @NotBlank, @Pattern ✓
       └─ country: @NotBlank, @Size ✓
        ↓
   All validations pass
        ↓
    HTTP 200 OK
```

## Test Cases

### Test 1: Valid Request with Nested Address

**Request:**

```bash
curl -X POST http://localhost:8080/users/endpoint1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+15551234567",
    "address": {
      "street": "123 Main Street",
      "city": "Springfield",
      "state": "IL",
      "postalCode": "62701",
      "country": "USA"
    }
  }'
```

**Response:** ✅ 200 OK

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+15551234567",
  "address": {
    "street": "123 Main Street",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

### Test 2: Invalid Nested Field (Short Street)

**Request:**

```bash
curl -X POST http://localhost:8080/users/endpoint1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+15551234567",
    "address": {
      "street": "123",
      "city": "Springfield",
      "state": "IL",
      "postalCode": "62701",
      "country": "USA"
    }
  }'
```

**Response:** ❌ 400 Bad Request

```json
{
  "address.street": "Street must be between 5 and 100 characters"
}
```

### Test 3: Invalid Nested Field (Invalid Postal Code)

**Request:**

```bash
curl -X POST http://localhost:8080/users/endpoint1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+15551234567",
    "address": {
      "street": "123 Main Street",
      "city": "Springfield",
      "state": "IL",
      "postalCode": "invalid",
      "country": "USA"
    }
  }'
```

**Response:** ❌ 400 Bad Request

```json
{
  "address.postalCode": "Postal code must be 3-10 alphanumeric characters (uppercase)"
}
```

### Test 4: Null Address (Endpoint1 Validation Group)

**Request:**

```bash
curl -X POST http://localhost:8080/users/endpoint1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+15551234567",
    "address": null
  }'
```

**Response:** ❌ 400 Bad Request

```json
{
  "address": "Address cannot be null"
}
```

### Test 5: Endpoint2 (Address NOT validated - different view)

**Request:**

```bash
curl -X POST http://localhost:8080/users/endpoint2 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "1990-05-15",
    "department": "Engineering",
    "salary": 85000.00
  }'
```

**Response:** ✅ 200 OK

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "birthDate": "1990-05-15",
  "department": "Engineering",
  "salary": 85000.00
}
```

**Note:** Address is completely excluded because it's only part of Endpoint1 view, so it's neither deserialized nor
validated for Endpoint2.

## Key Points

### How @Valid and @Validated Work Together

| Scenario                                              | Behavior                     |
|-------------------------------------------------------|------------------------------|
| @Validated on parameter + @Valid on nested field      | Cascading validation enabled |
| @Validated on parameter but NO @Valid on nested field | Nested object NOT validated  |
| NO @Validated on parameter + @Valid on nested field   | No validation occurs         |
| Both missing                                          | No validation occurs         |

### Validation Groups + Cascading

```java
// Endpoint1 - Address is validated because it's in Endpoint1Validation group
@Validated(Views.ValidationGroups.Endpoint1Validation.class)
public ResponseEntity<User> createUserEndpoint1(...User user)

// Endpoint2 - Address is completely ignored because it's only in Endpoint1 view
@Validated(Views.ValidationGroups.Endpoint2Validation.class)
public ResponseEntity<User> createUserEndpoint2(...User user)
```

## Summary

✅ **Cascading validation** automatically validates nested objects  
✅ **@Valid annotation** triggers cascading for that specific field  
✅ **Nested errors** show path (e.g., `address.street`)  
✅ **Works with @JsonView** - Only nested objects in active view are validated  
✅ **Works with validation groups** - Nested objects validated only when parent group applies

## Valid Postal Code Examples

```
// US Formats
12345
90210
10001

// International Formats
M5H2N2    (Canada)
SW1A1AA   (UK)
75001     (France)
10115     (Germany)
T1X1V1    (Canada)
B1A2B3    (Canada)
SE3

E1A2B3
```

The implementation is now complete with cascading validation! 🚀
