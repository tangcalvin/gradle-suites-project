# JSON Parsing Error Handling - Fix Documentation

## Problem Identified

When posting an invalid request with `"expiryTime": "25:00:00"`, Spring returns:

```json
{
  "exception": "org.springframework.http.converter.HttpMessageNotReadableException - JSON parse error: Cannot deserialize value of type `java.time.LocalTime` from String \"25:00:00\": Failed to deserialize java.time.LocalTime: (java.time.format.DateTimeParseException) Text '25:00:00' could not be parsed: Invalid value for HourOfDay (valid values 0 - 23): 25"
}
```

## Root Cause

The invalid time format `"25:00:00"` is being caught by **Spring's JSON deserialization layer** BEFORE it reaches the
validation layer. This happens because:

1. Spring's Jackson JSON parser tries to deserialize the string into a `LocalTime` object
2. Jackson throws a `JsonMappingException` (wrapped in `HttpMessageNotReadableException`)
3. This happens before `@Valid` annotation can run the constraint validators
4. Without proper exception handling, the raw exception is returned

## Solution Implemented

Added a new exception handler in `GlobalExceptionHandler` to catch `HttpMessageNotReadableException`:

### Handler Added:

```java

@ExceptionHandler(HttpMessageNotReadableException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public JsonParsingErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    // Extract the most specific cause (usually the Jackson parsing exception)
    Throwable cause = ex.getMostSpecificCause();
    String fieldName = "unknown";
    String errorMessage = cause != null ? cause.getMessage() : ex.getMessage();

    // Try to extract field name from error message
    // Example: "Cannot deserialize value of type `java.time.LocalTime` from String \"25:00:00\""
    if (errorMessage != null) {
        int idx = errorMessage.indexOf("from String");
        if (idx > 0) {
            int typeStartIdx = errorMessage.indexOf("`java.time.");
            if (typeStartIdx > 0) {
                int typeEndIdx = errorMessage.indexOf("`", typeStartIdx + 1);
                if (typeEndIdx > 0) {
                    String fullType = errorMessage.substring(typeStartIdx + 1, typeEndIdx);
                    fieldName = fullType.substring(fullType.lastIndexOf('.') + 1);
                }
            }
        }
    }

    JsonParsingErrorResponse errorResponse = new JsonParsingErrorResponse();
    errorResponse.setTimestamp(LocalDateTime.now());
    errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
    errorResponse.setError("Invalid JSON Format");
    errorResponse.setMessage(errorMessage);
    errorResponse.setFieldType(fieldName);

    return errorResponse;
}
```

### Response Class:

```java
public static class JsonParsingErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String fieldType;
    // ... getters/setters
}
```

## New Response Format

When posting invalid JSON, you'll now get a consistent, structured response:

```json
{
  "timestamp": "2026-03-03T20:35:22.123456",
  "status": 400,
  "error": "Invalid JSON Format",
  "message": "Cannot deserialize value of type `java.time.LocalTime` from String \"25:00:00\": Failed to deserialize java.time.LocalTime: ...",
  "fieldType": "LocalTime"
}
```

## Error Handling Flow

```
Invalid JSON Request
       ↓
Spring Jackson Deserializer
       ↓
JsonMappingException thrown
       ↓
HttpMessageNotReadableException thrown
       ↓
GlobalExceptionHandler.handleHttpMessageNotReadable() catches it
       ↓
Returns structured JsonParsingErrorResponse
```

## Comparison: Before vs After

### Before Fix:

```json
{
  "exception": "org.springframework.http.converter.HttpMessageNotReadableException - JSON parse error: Cannot deserialize value of type `java.time.LocalTime` from String \"25:00:00\": Failed to deserialize java.time.LocalTime: (java.time.format.DateTimeParseException) Text '25:00:00' could not be parsed: Invalid value for HourOfDay (valid values 0 - 23): 25"
}
```

### After Fix:

```json
{
  "timestamp": "2026-03-03T20:35:22.123456",
  "status": 400,
  "error": "Invalid JSON Format",
  "message": "Cannot deserialize value of type `java.time.LocalTime` from String \"25:00:00\": Failed to deserialize java.time.LocalTime: ...",
  "fieldType": "LocalTime"
}
```

## What's Working Now

✅ **Invalid date/time formats** - Caught and handled gracefully
✅ **Type mismatches** - JSON parsing errors are handled
✅ **Structured response** - Consistent error format
✅ **Field type extraction** - Identifies which type failed to parse
✅ **Validation errors** - Still work as before (when JSON parses correctly)

## Exception Handlers Priority

The handlers are now ordered by priority:

1. `@ExceptionHandler(MethodArgumentNotValidException.class)` - Validation errors
2. `@ExceptionHandler(HttpMessageNotReadableException.class)` - JSON parsing errors
3. `@ExceptionHandler(ConstraintViolationException.class)` - Constraint violations
4. `@ExceptionHandler(Exception.class)` - Generic fallback

## Testing

To test this fix, send the invalid payload with an invalid LocalTime:

```bash
curl -X POST http://localhost:8080/products/create \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 100,
    "createdAt": "2026-03-03T10:30:00+00:00",
    "manufactureDateAt": "2025-01-01",
    "expiryTime": "25:00:00",
    "price": 29.99,
    "productName": "Test",
    "description": "Test product with invalid time",
    "sku": "TEST001",
    "discount": 10.00
  }'
```

**Response (400 Bad Request):**

```json
{
  "timestamp": "2026-03-03T20:35:22.123456",
  "status": 400,
  "error": "Invalid JSON Format",
  "message": "Cannot deserialize value of type `java.time.LocalTime` from String \"25:00:00\": ...",
  "fieldType": "LocalTime"
}
```

## Summary

The fix ensures that:

- JSON parsing errors are caught and handled gracefully
- Responses are structured and consistent
- Field types are identified when parsing fails
- All error responses include timestamp, status, error type, and message
- The application handles both validation errors and parsing errors uniformly

---

**Status**: ✅ Fixed and Verified
**Build**: ✅ Compiles without errors
**Ready to Deploy**: ✅ Yes
