package com.philomath.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Custom deserializer for ProductDTO that validates all fields
 * and collects all constraint violations together, including type mismatches.
 */
public class ProductDTODeserializer extends JsonDeserializer<ProductDTO> {

    @Override
    public ProductDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ProductDTO dto = new ProductDTO();
        Map<String, List<String>> violations = new HashMap<>();

        // Try to parse each field individually and collect errors
        if (node.has("productId")) {
            try {
                if (node.get("productId").isNumber()) {
                    dto.setProductId(node.get("productId").asLong());
                } else {
                    dto.setProductId(null);
                    violations.computeIfAbsent("productId", k -> new ArrayList<>())
                            .add("Cannot deserialize value of type `java.lang.Long` from String \"" + node.get("productId").asText() + "\": not a valid `java.lang.Long` value");
                }
            } catch (Exception e) {
                dto.setProductId(null);
                violations.computeIfAbsent("productId", k -> new ArrayList<>())
                        .add("Invalid value for productId: " + e.getMessage());
            }
        }

        if (node.has("quantity")) {
            try {
                if (node.get("quantity").isNumber()) {
                    dto.setQuantity(node.get("quantity").asInt());
                } else {
                    dto.setQuantity(null);
                    violations.computeIfAbsent("quantity", k -> new ArrayList<>())
                            .add("Cannot deserialize value of type `java.lang.Integer` from String \"" + node.get("quantity").asText() + "\": not a valid `java.lang.Integer` value");
                }
            } catch (Exception e) {
                dto.setQuantity(null);
                violations.computeIfAbsent("quantity", k -> new ArrayList<>())
                        .add("Invalid value for quantity: " + e.getMessage());
            }
        }

        if (node.has("createdAt")) {
            try {
                dto.setCreatedAt(OffsetDateTime.parse(node.get("createdAt").asText()));
            } catch (Exception e) {
                dto.setCreatedAt(null);
                violations.computeIfAbsent("createdAt", k -> new ArrayList<>())
                        .add("Cannot deserialize value of type `java.time.OffsetDateTime`: " + e.getMessage());
            }
        }

        if (node.has("manufactureDateAt")) {
            try {
                dto.setManufactureDateAt(LocalDate.parse(node.get("manufactureDateAt").asText()));
            } catch (Exception e) {
                dto.setManufactureDateAt(null);
                violations.computeIfAbsent("manufactureDateAt", k -> new ArrayList<>())
                        .add("Cannot deserialize value of type `java.time.LocalDate`: " + e.getMessage());
            }
        }

        if (node.has("expiryTime")) {
            try {
                dto.setExpiryTime(LocalTime.parse(node.get("expiryTime").asText()));
            } catch (Exception e) {
                dto.setExpiryTime(null);
                violations.computeIfAbsent("expiryTime", k -> new ArrayList<>())
                        .add("Cannot deserialize value of type `java.time.LocalTime`: " + e.getMessage());
            }
        }

        if (node.has("price")) {
            try {
                if (node.get("price").isNumber()) {
                    dto.setPrice(new BigDecimal(node.get("price").asText()));
                } else {
                    dto.setPrice(null);
                    violations.computeIfAbsent("price", k -> new ArrayList<>())
                            .add("Cannot deserialize value of type `java.math.BigDecimal`: not a valid number");
                }
            } catch (Exception e) {
                dto.setPrice(null);
                violations.computeIfAbsent("price", k -> new ArrayList<>())
                        .add("Invalid value for price: " + e.getMessage());
            }
        }

        if (node.has("productName")) {
            try {
                dto.setProductName(node.get("productName").asText());
            } catch (Exception e) {
                violations.computeIfAbsent("productName", k -> new ArrayList<>())
                        .add("Invalid value for productName: " + e.getMessage());
            }
        }

        if (node.has("description")) {
            try {
                dto.setDescription(node.get("description").asText());
            } catch (Exception e) {
                violations.computeIfAbsent("description", k -> new ArrayList<>())
                        .add("Invalid value for description: " + e.getMessage());
            }
        }

        if (node.has("sku")) {
            try {
                dto.setSku(node.get("sku").asText());
            } catch (Exception e) {
                violations.computeIfAbsent("sku", k -> new ArrayList<>())
                        .add("Invalid value for sku: " + e.getMessage());
            }
        }

        if (node.has("discount")) {
            try {
                if (node.get("discount").isNumber()) {
                    dto.setDiscount(new BigDecimal(node.get("discount").asText()));
                } else {
                    dto.setDiscount(null);
                    violations.computeIfAbsent("discount", k -> new ArrayList<>())
                            .add("Cannot deserialize value of type `java.math.BigDecimal`: not a valid number");
                }
            } catch (Exception e) {
                dto.setDiscount(null);
                violations.computeIfAbsent("discount", k -> new ArrayList<>())
                        .add("Invalid value for discount: " + e.getMessage());
            }
        }

        // Validate constraints even if there were deserialization errors
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            Set<ConstraintViolation<ProductDTO>> constraintViolations = validator.validate(dto);
            for (ConstraintViolation<ProductDTO> violation : constraintViolations) {
                violations.computeIfAbsent(violation.getPropertyPath().toString(), k -> new ArrayList<>())
                        .add(violation.getMessage());
            }
        }

        // If there are any violations, throw a custom exception
        if (!violations.isEmpty()) {
            throw new ProductDTOValidationException(violations);
        }

        return dto;
    }
}
