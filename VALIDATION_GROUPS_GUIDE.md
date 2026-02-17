# Validation Groups Implementation - Complete Guide

## Problem Solved

Previously, when calling endpoint2, it was validating `phone` and `address` fields (which belong to endpoint1), causing
400 errors even though those fields weren't part of the endpoint2 request.

**Root Cause:** Jakarta validation validates ALL fields with validation annotations, regardless of @JsonView. @JsonView
only controls serialization/deserialization, not validation.

## Solution: Validation Groups

We implemented **Jakarta Validation Groups** to ensure:

- Only fields relevant to an endpoint are validated
- Endpoint1 validates phone & address (but not birthDate, department, salary)
- Endpoint2 validates birthDate, department, salary (but not phone & address)

## How It Works

### 1. Validation Group Interfaces (Views.java)

```java
public static class ValidationGroups {
    public interface Endpoint1Validation {
    }

    public interface Endpoint2Validation {
    }
}
```

These are marker interfaces that categorize which validations apply to which endpoint.

### 2. Field-Level Validation Groups (User.java)

**Endpoint 1 Specific Fields:**

```java

@NotBlank(message = "Phone number cannot be blank",
        groups = Views.ValidationGroups.Endpoint1Validation.class)
@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
        message = "Phone number must be a valid E.164 format",
        groups = Views.ValidationGroups.Endpoint1Validation.class)
@JsonView(Views.Endpoint1.class)
private String phone;
```

**Endpoint 2 Specific Fields:**

```java

@NotBlank(message = "Birth date cannot be blank",
        groups = Views.ValidationGroups.Endpoint2Validation.class)
@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "Birth date must be in yyyy-MM-dd format",
        groups = Views.ValidationGroups.Endpoint2Validation.class)
@JsonView(Views.Endpoint2.class)
private String birthDate;
```

**Common Fields:** No groups specified = Always validated (for both endpoints)

### 3. Controller-Level Activation (UserController.java)

```java

@Validated  // Enable method-level validation
@RestController
public class UserController {

    @PostMapping("/endpoint1")
    public ResponseEntity<User> createUserEndpoint1(
            @Validated(Views.ValidationGroups.Endpoint1Validation.class)
            @RequestBody User user) {
        return ResponseEntity.ok(user);
    }

    @PostMapping("/endpoint2")
    public ResponseEntity<User> createUserEndpoint2(
            @Validated(Views.ValidationGroups.Endpoint2Validation.class)
            @RequestBody User user) {
        return ResponseEntity.ok(user);
    }
}
```

The `@Validated` annotation on the parameter activates only the specified validation group.

## Validation Behavior Matrix

| Field      | Endpoint1   | Endpoint2   | Reason                         |
|------------|-------------|-------------|--------------------------------|
| id         | ✅ Validated | ✅ Validated | No group = always validated    |
| username   | ✅ Validated | ✅ Validated | No group = always validated    |
| email      | ✅ Validated | ✅ Validated | No group = always validated    |
| firstName  | ✅ Validated | ✅ Validated | No group = always validated    |
| lastName   | ✅ Validated | ✅ Validated | No group = always validated    |
| phone      | ✅ Validated | ❌ SKIPPED   | Endpoint1Validation group only |
| address    | ✅ Validated | ❌ SKIPPED   | Endpoint1Validation group only |
| birthDate  | ❌ SKIPPED   | ✅ Validated | Endpoint2Validation group only |
| department | ❌ SKIPPED   | ✅ Validated | Endpoint2Validation group only |
| salary     | ❌ SKIPPED   | ✅ Validated | Endpoint2Validation group only |

## Testing

### Endpoint 1 - Valid Request

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
    "address": "123 Main Street, Springfield, IL 62701"
  }'
```

**Result:** ✅ 200 OK

### Endpoint 2 - Valid Request (phone & address NOT required)

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

**Result:** ✅ 200 OK (phone & address validation is SKIPPED)

### Endpoint 2 - Invalid birthDate (still validated)

```bash
curl -X POST http://localhost:8080/users/endpoint2 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "invalid-date",
    "department": "Engineering",
    "salary": 85000.00
  }'
```

**Result:** ❌ 400 Bad Request

```json
{
  "birthDate": "Birth date must be in yyyy-MM-dd format"
}
```

### Endpoint 1 - Missing phone (Endpoint1Validation group applies)

```bash
curl -X POST http://localhost:8080/users/endpoint1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "address": "123 Main Street, Springfield, IL 62701"
  }'
```

**Result:** ❌ 400 Bad Request

```json
{
  "phone": "Phone number cannot be blank"
}
```

## Key Concepts

### Groups in Jakarta Validation

- **Default Group (default):** Always applied when no group is specified
- **Custom Groups:** Created by defining marker interfaces
- **Group Activation:** Happens via `@Validated(GroupInterface.class)` annotation

### When Validation Runs

1. Controller receives request
2. Spring sees `@Validated(Endpoint1Validation.class)`
3. Jackson deserializes JSON based on @JsonView
4. Jakarta validates ONLY fields in Endpoint1Validation group + fields with no group
5. Validation fails if any field violates its constraints
6. Spring returns 400 with error details OR 200 with validated object

### Why Both @JsonView AND Validation Groups?

| Annotation        | Purpose                                           |
|-------------------|---------------------------------------------------|
| @JsonView         | Controls which fields are serialized/deserialized |
| Validation Groups | Controls which fields are validated               |

Both are needed because:

- @JsonView = "What data is exchanged?"
- Validation Groups = "What data is validated?"

They work together to provide complete endpoint-specific contracts.

## Benefits

✅ **Single DTO for multiple endpoints** - Reuse without duplication
✅ **Type-safe contracts** - Compile-time checking
✅ **Clear validation rules** - Each field has explicit constraints per endpoint
✅ **No validation pollution** - Endpoint1 doesn't validate endpoint2 fields
✅ **Automatic error handling** - Spring provides proper HTTP responses
✅ **Swagger integration** - OpenAPI shows correct validations per endpoint

## File Changes Summary

### Views.java

Added `ValidationGroups` nested class with:

- `Endpoint1Validation` interface
- `Endpoint2Validation` interface

### User.java

Updated validation annotations to include `groups` parameter:

- Endpoint1 fields → `groups = Views.ValidationGroups.Endpoint1Validation.class`
- Endpoint2 fields → `groups = Views.ValidationGroups.Endpoint2Validation.class`
- Common fields → No group parameter (always validated)

### UserController.java

Updated method signatures:

- Added `@Validated` class-level annotation
- Changed `@Valid` to `@Validated(ValidationGroup)` on parameters
- Endpoint1 uses `Endpoint1Validation` group
- Endpoint2 uses `Endpoint2Validation` group

The implementation is now complete and production-ready! 🚀
