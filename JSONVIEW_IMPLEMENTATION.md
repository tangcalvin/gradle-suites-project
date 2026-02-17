# User DTO with @JsonView Implementation

## Overview

Created a complete User DTO with 10 attributes and a @RestController with two endpoints demonstrating @JsonView
functionality for conditional field serialization.

## Files Created

### 1. User.java (DTO)

**Location:** `microservice-1/src/main/java/com/philomath/dto/User.java`

**10 Attributes:**

1. **id** (Long) - Common attribute
2. **username** (String) - Common attribute
3. **email** (String) - Common attribute
4. **firstName** (String) - Common attribute
5. **lastName** (String) - Common attribute
6. **phone** (String) - Endpoint 1 specific
7. **address** (String) - Endpoint 1 specific
8. **birthDate** (String) - Endpoint 2 specific
9. **department** (String) - Endpoint 2 specific
10. **salary** (BigDecimal) - Endpoint 2 specific

**JsonView Configuration:**

- Attributes 1-5: `@JsonView({Views.Endpoint1.class, Views.Endpoint2.class})` - Shown in both endpoints
- Attributes 6-7: `@JsonView(Views.Endpoint1.class)` - Only shown in Endpoint 1
- Attributes 8-10: `@JsonView(Views.Endpoint2.class)` - Only shown in Endpoint 2

### 2. Views.java (JsonView Interface)

**Location:** `microservice-1/src/main/java/com/philomath/dto/Views.java`

Defines two view interfaces:

- `Views.Endpoint1` - For Endpoint 1 serialization
- `Views.Endpoint2` - For Endpoint 2 serialization

### 3. UserController.java (RestController)

**Location:** `microservice-1/src/main/java/com/philomath/controller/UserController.java`

**Base Path:** `/users`

**Endpoint 1:**

- **URL:** `GET /users/endpoint1/{id}`
- **Method:** `getUserEndpoint1(Long id)`
- **JsonView:** `Views.Endpoint1.class`
- **Returns:** User with attributes 1-7 (excludes 8, 9, 10)
- **Use Case:** Returns user contact information

**Endpoint 2:**

- **URL:** `GET /users/endpoint2/{id}`
- **Method:** `getUserEndpoint2(Long id)`
- **JsonView:** `Views.Endpoint2.class`
- **Returns:** User with attributes 1-5, 8-10 (excludes 6, 7)
- **Use Case:** Returns user employment information

## JSON Response Examples

### Endpoint 1 Response (`/users/endpoint1/1`)

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1-555-123-4567",
  "address": "123 Main Street, Springfield, IL 62701"
}
```

### Endpoint 2 Response (`/users/endpoint2/1`)

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

## How @JsonView Works

@JsonView annotations control which fields are included in JSON serialization based on the active view:

1. When a method is annotated with `@JsonView(Views.Endpoint1.class)`, Jackson only serializes fields marked with that
   view
2. Fields marked with multiple views (like common attributes) appear in all those views
3. Fields marked with other views (like phone for Endpoint1) are excluded

This provides a clean way to return different data subsets from the same DTO without creating separate DTOs or complex
conditional logic.

## Testing the Endpoints

```bash
# Endpoint 1 - Contact Information
curl http://localhost:8080/users/endpoint1/1

# Endpoint 2 - Employment Information
curl http://localhost:8080/users/endpoint2/1
```

## Dependencies Used

- Spring Boot Web (for @RestController, @GetMapping, ResponseEntity)
- Jackson (for @JsonView) - included with Spring Boot Web starter
