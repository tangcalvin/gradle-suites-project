package com.philomath.controller;


import com.philomath.dto.ProductDTOValidationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Payload;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Global exception handler
@RestControllerAdvice(basePackages = "com.philomath.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductDTOValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleProductDTOValidationException(ProductDTOValidationException ex) {
        Map<String, List<String>> violations = ex.getViolations();

        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError("Validation Failed");
        errorResponse.setMessage("Please correct the following field errors");
        errorResponse.setViolations(violations);
        errorResponse.setTotalViolations(
                violations.values().stream().mapToInt(List::size).sum()
        );

        return errorResponse;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        Map<String, String> errorMessages = new HashMap<>();
        ex.visitResults(new HandlerMethodValidationException.Visitor() {

            @Override
            public void requestParam(RequestParam requestParam, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void requestPart(RequestPart requestPart, ParameterErrors errors) {
                for (ObjectError error : errors.getAllErrors()) {
                    errorMessages.put(error.getObjectName(), error.getDefaultMessage());
                }
            }

            @Override
            public void requestHeader(RequestHeader requestHeader, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void cookieValue(CookieValue cookieValue, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void matrixVariable(MatrixVariable matrixVariable, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void modelAttribute(ModelAttribute modelAttribute, ParameterErrors errors) {
                // Iterate over List<ObjectError> from getAllErrors()
                for (ObjectError error : errors.getAllErrors()) {
                    errorMessages.put(error.getObjectName(), error.getDefaultMessage());
                }
            }

            @Override
            public void pathVariable(PathVariable pathVariable, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void requestBody(RequestBody requestBody, ParameterErrors errors) {
                for (ObjectError error : errors.getAllErrors()) {
                    errorMessages.put(error.getObjectName(), error.getDefaultMessage());
                }
            }

            @Override
            public void other(ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            private void addMessagesFromResult(ParameterValidationResult result) {
                List<? extends MessageSourceResolvable> errors = result.getResolvableErrors();
                for (MessageSourceResolvable error : errors) {
                    errorMessages.put(error.getClass().getName(), error.getDefaultMessage());
                }
            }
        });
        return errorMessages;
    }

    // Handle MethodArgumentNotValidException - Collect all field violations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // Collect all constraint violations grouped by field name
        Map<String, List<String>> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        fieldError -> fieldError.getField(),
                        Collectors.mapping(
                                fieldError -> fieldError.getDefaultMessage(),
                                Collectors.toList()
                        )
                ));

        // Create structured error response
        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError("Validation Failed");
        errorResponse.setMessage("Please correct the following field errors");
        errorResponse.setViolations(violations);
        errorResponse.setTotalViolations(
                violations.values().stream().mapToInt(List::size).sum()
        );

        return errorResponse;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            // Get the payload class
            Optional<Class<? extends Payload>> payloadClass = cv.getConstraintDescriptor().getPayload().stream().findFirst();
            String payloadClassname = payloadClass.map(Class::getSimpleName).orElse(null);
            // Combine the payload class name with the message
            String errorMessage = payloadClassname != null ?
                    payloadClassname + ": " + cv.getMessage() : cv.getMessage();
            errors.put(cv.getPropertyPath().toString(), errorMessage);
        });
        return errors;
    }

    /**
     * Handle HttpMessageNotReadableException - JSON parsing errors (e.g., invalid date/time formats)
     * This catches errors like "25:00:00" for LocalTime before they reach the validator
     */
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

    /**
     * Fallback handler for other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneralException(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getClass().getSimpleName());
        errors.put("message", ex.getMessage());
        return errors;
    }

    /**
     * Inner class for structured validation error response
     */
    public static class ValidationErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, List<String>> violations;
        private int totalViolations;

        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, List<String>> getViolations() {
            return violations;
        }

        public void setViolations(Map<String, List<String>> violations) {
            this.violations = violations;
        }

        public int getTotalViolations() {
            return totalViolations;
        }

        public void setTotalViolations(int totalViolations) {
            this.totalViolations = totalViolations;
        }
    }

    /**
     * Inner class for JSON parsing error response
     */
    public static class JsonParsingErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String fieldType;

        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }
    }
}
