package com.philomath.dto;

import java.util.List;
import java.util.Map;

/**
 * Custom exception for ProductDTO validation that contains all violations
 * (both deserialization errors and constraint violations).
 */
public class ProductDTOValidationException extends RuntimeException {
    private final Map<String, List<String>> violations;

    public ProductDTOValidationException(Map<String, List<String>> violations) {
        super("ProductDTO validation failed with " + violations.values().stream().mapToInt(List::size).sum() + " violation(s)");
        this.violations = violations;
    }

    public Map<String, List<String>> getViolations() {
        return violations;
    }
}
