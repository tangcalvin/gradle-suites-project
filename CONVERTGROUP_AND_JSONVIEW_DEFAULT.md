# @ConvertGroup with Multiple Groups & @JsonView Default Behavior

## 1. @ConvertGroup with Multiple Groups

### Question: Can @ConvertGroup support multiple groups?

**Answer:** Yes, but not directly in a single annotation. You have two approaches:

### Approach 1: Stack Multiple @ConvertGroup Annotations

You can apply multiple `@ConvertGroup` annotations to convert different validation groups to the Default group:

```java

@Valid
@ConvertGroup(from = Views.ValidationGroups.Endpoint1Validation.class, to = Default.class)
@ConvertGroup(from = Views.ValidationGroups.Endpoint2Validation.class, to = Default.class)
@NotNull(message = "Address cannot be null")
private Address address;
```

**How It Works:**

- First `@ConvertGroup`: When parent uses `Endpoint1Validation`, convert to `Default` for nested validation
- Second `@ConvertGroup`: When parent uses `Endpoint2Validation`, convert to `Default` for nested validation
- Result: Address is validated in both endpoint contexts

**Use Case:**

```java
// Endpoint 1 - First @ConvertGroup is active
@Validated(Views.ValidationGroups.Endpoint1Validation.class)
public ResponseEntity<User> createUserEndpoint1(@RequestBody User user)

// Endpoint 2 - Second @ConvertGroup is active
@Validated(Views.ValidationGroups.Endpoint2Validation.class)
public ResponseEntity<User> createUserEndpoint2(@RequestBody User user)
```

### Approach 2: Composed Validation Groups (Recommended)

Create a composed group that includes multiple groups:

```java
public static class ValidationGroups {
    public interface Endpoint1Validation {
    }

    public interface Endpoint2Validation {
    }

    // Composed group - includes both
    public interface Combined extends Endpoint1Validation, Endpoint2Validation {
    }
}
```

**Usage:**

```java

@Valid
@ConvertGroup(from = Views.ValidationGroups.Combined.class, to = Default.class)
private Address address;
```

**Benefit:** Single `@ConvertGroup` that handles multiple validation scenarios

### Practical Example: Using Composed Groups

**Views.java:**

```java
public static class ValidationGroups {
    public interface Endpoint1Validation {
    }

    public interface Endpoint2Validation {
    }

    // Validates fields from BOTH Endpoint1 and Endpoint2
    public interface Combined extends Endpoint1Validation, Endpoint2Validation {
    }
}
```

**User.java:**

```java

@Valid
@ConvertGroup(from = Views.ValidationGroups.Combined.class, to = Default.class)
@NotNull(message = "Address cannot be null")
private Address address;
```

**Controller.java:**

```java

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
```

---

## 2. @JsonView Default - Always Render Fields

### Question: Is there a Default for @JsonView so fields are always rendered?

**Answer:** YES! I've added `Views.Default` interface specifically for this purpose.

### The Problem:

By default, if a field doesn't have ANY `@JsonView` annotation, it's rendered in ALL views. But if you want a field to
always be rendered regardless of the active view, you need an explicit "Default" view.

### The Solution: Views.Default

**Updated Views.java includes:**

```java
/**
 * Default view interface - fields marked with this are always serialized
 * in all view contexts (similar to Default validation group concept)
 */
public interface Default {
}
```

### How to Use Views.Default

**Example 1: Always Render ID**

```java

@NotNull(message = "User ID cannot be null")
@Positive(message = "User ID must be a positive number")
@JsonView({Views.Endpoint1.class, Views.Endpoint2.class, Views.Default.class})
private Long id;
```

**Example 2: System Fields (Timestamps, Metadata)**

```java

@JsonView(Views.Default.class)  // Always rendered
private LocalDateTime createdAt;

@JsonView(Views.Default.class)  // Always rendered
private LocalDateTime updatedAt;

@JsonView(Views.Default.class)  // Always rendered
private String createdBy;
```

### Three Patterns for @JsonView

| Pattern                                                     | When Field is Rendered | Use Case                     |
|-------------------------------------------------------------|------------------------|------------------------------|
| `@JsonView(Views.Endpoint1.class)`                          | Only for Endpoint1     | Endpoint-specific data       |
| `@JsonView({Views.Endpoint1.class, Views.Endpoint2.class})` | For both endpoints     | Common fields needed by both |
| `@JsonView(Views.Default.class)`                            | All views always       | System metadata, IDs         |

### Updated User Model Example

```java
public class User {
    // System metadata - always rendered
    @JsonView(Views.Default.class)
    private Long id;

    @JsonView(Views.Default.class)
    private LocalDateTime createdAt;

    @JsonView(Views.Default.class)
    private LocalDateTime updatedAt;

    // Common business fields - both endpoints
    @JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
    private String username;

    @JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
    private String email;

    // Endpoint 1 specific
    @JsonView(Views.Endpoint1.class)
    private String phone;

    @JsonView(Views.Endpoint1.class)
    private Address address;

    // Endpoint 2 specific
    @JsonView(Views.Endpoint2.class)
    private String birthDate;

    @JsonView(Views.Endpoint2.class)
    private String department;

    @JsonView(Views.Endpoint2.class)
    private BigDecimal salary;
}
```

**Endpoint 1 Response (includes Default + Endpoint1 views):**

```json
{
  "id": 1,
  "createdAt": "2026-02-17T10:30:00Z",
  "updatedAt": "2026-02-17T10:30:00Z",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "phone": "+15551234567",
  "address": {
    ...
  }
}
```

**Endpoint 2 Response (includes Default + Endpoint2 views):**

```json
{
  "id": 1,
  "createdAt": "2026-02-17T10:30:00Z",
  "updatedAt": "2026-02-17T10:30:00Z",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "birthDate": "1990-05-15",
  "department": "Engineering",
  "salary": 85000.00
}
```

---

## Complete Example: Both Concepts Together

### Views.java (Updated)

```java
public class Views {
    public interface Endpoint1 {
    }

    public interface Endpoint2 {
    }

    public interface Default {
    }  // ← New: Always rendered fields

    public static class ValidationGroups {
        public interface Endpoint1Validation {
        }

        public interface Endpoint2Validation {
        }

        public interface Combined extends Endpoint1Validation, Endpoint2Validation {
        }  // ← New: Composed groups
    }
}
```

### User.java (Updated)

```java

@Valid
@ConvertGroup(from = Views.ValidationGroups.Endpoint1Validation.class, to = Default.class)
@ConvertGroup(from = Views.ValidationGroups.Endpoint2Validation.class, to = Default.class)
@NotNull(message = "Address cannot be null")
@JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
private Address address;
```

### Controller.java

```java

@PostMapping("/endpoint1")
public ResponseEntity<User> createUserEndpoint1(
        @Validated(Views.ValidationGroups.Endpoint1Validation.class)
        @RequestBody @JsonView(Views.Endpoint1.class) User user) {
    return ResponseEntity.ok(user);
}

@PostMapping("/endpoint2")
public ResponseEntity<User> createUserEndpoint2(
        @Validated(Views.ValidationGroups.Endpoint2Validation.class)
        @RequestBody @JsonView(Views.Endpoint2.class) User user) {
    return ResponseEntity.ok(user);
}
```

---

## Key Differences: Default Validation vs Default JsonView

| Aspect       | Default Validation Group                        | Views.Default                        |
|--------------|-------------------------------------------------|--------------------------------------|
| **Purpose**  | Fields validated when no group specified        | Fields rendered in all view contexts |
| **Source**   | `jakarta.validation.groups.Default`             | `Views.Default` (custom)             |
| **Usage**    | `groups = Default.class`                        | `@JsonView(Views.Default.class)`     |
| **Behavior** | Implicit - auto-included if no groups specified | Explicit - must be explicitly marked |
| **Scope**    | Validation only                                 | Serialization only                   |

---

## Summary

✅ **Multiple @ConvertGroup:** Use stacked annotations or composed groups  
✅ **@JsonView Default:** Use `Views.Default` for always-rendered fields  
✅ **Composed Groups:** Create `Combined` group extending multiple groups  
✅ **Best Practice:** Use `Views.Default` for system metadata, IDs, timestamps

Both features provide powerful fine-grained control over what gets validated and what gets serialized! 🚀
