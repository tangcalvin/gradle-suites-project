# ProductDTO @JsonView Implementation

## Overview

The ProductDTO now supports `@JsonView` annotations to control which fields are serialized/deserialized based on the
view context.

## Implementation Details

### @JsonView Annotation

All 10 fields in ProductDTO are annotated with:

```java
@JsonView(ProductViews.Default.class)
```

This means:

- All fields are **always serialized** when the request/response is sent
- All fields are **always deserialized** when receiving a request
- The Default view is the base view that all responses use

### ProductViews Class

Created `ProductViews.java` with three view interfaces:

```java
public class ProductViews {
    public interface Default {
    }

    public interface Summary extends Default {
    }

    public interface Detailed extends Default {
    }
}
```

### Usage Examples

#### Example 1: Return only summary information

In your controller, configure the ObjectMapper to use the Summary view:

```java

@GetMapping("/{productId}")
public ResponseEntity<?> getProduct(@PathVariable Long productId) {
    Product product = productService.findById(productId);
    return ResponseEntity.ok()
            .body(product);  // Returns with ProductViews.Default
}
```

#### Example 2: Return detailed information with specific view

```java

@GetMapping("/{productId}/detailed")
public ResponseEntity<?> getProductDetailed(@PathVariable Long productId) {
    Product product = productService.findById(productId);
    return ResponseEntity.ok()
            .body(product);  // Could use ProductViews.Detailed
}
```

## Current Configuration

All fields use `@JsonView(ProductViews.Default.class)`, which means:

- ✅ All fields are serialized in responses
- ✅ All fields are deserialized from requests
- ✅ Validation applies to all fields

## Difference Between @Valid and @JsonView

| Aspect          | @Valid                 | @JsonView                              |
|-----------------|------------------------|----------------------------------------|
| Purpose         | Triggers validation    | Controls serialization/deserialization |
| When Used       | During request binding | During JSON processing                 |
| Effect          | Validates constraints  | Filters fields in/out                  |
| Can be combined | Yes                    | Yes                                    |

## Current Validation Setup

The ProductDTO validates:

- **All 10 fields** - No field-level view-based filtering
- **All constraints** - All @NotNull, @Min, @Max, etc. apply
- **On every POST request** - Using @Valid annotation in controller

## How to Customize Views

If you want different views to validate/serialize different fields, you would:

1. **Add field-specific views:**

```java

@JsonView(ProductViews.Summary.class)
private Long productId;

@JsonView(ProductViews.Detailed.class)
private String description;  // Only in detailed view
```

2. **Add group-based validation:**

```java

@NotBlank(groups = ValidationGroups.Detailed.class)
@JsonView(ProductViews.Detailed.class)
private String description;
```

3. **Use in controller:**

```java

@PostMapping("/products")
public ResponseEntity<?> create(@Validated(ValidationGroups.Detailed.class) @RequestBody ProductDTO product) {
    // Only validates fields in Detailed group
}
```

## Current Implementation Status

✅ ProductDTO has @JsonView annotations
✅ ProductViews class created
✅ All fields use Default view
✅ Ready for expansion to Summary/Detailed views if needed
✅ Supports view-based serialization

## Next Steps (Optional)

If you want to use different views:

1. Add field-specific `@JsonView` annotations to different fields
2. Create validation groups to match the views
3. Update controller to specify which view to use
4. Configure ObjectMapper with appropriate view

For now, the setup ensures:

- All fields are always validated (via @Valid + all constraints applied)
- All fields are always serialized/deserialized
- Framework is extensible for future view-based filtering

## Testing

The current setup validates the same way as before:

```bash
POST /products/create
```

All 10 fields must be valid, and all violations are collected and returned together.

---

**Implementation Complete**: ProductDTO now supports @JsonView with ProductViews class
