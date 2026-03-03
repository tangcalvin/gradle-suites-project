package com.philomath.dto;

import java.lang.annotation.*;

/**
 * Custom @JsonValidInput annotation to mark a field as requiring validation during JSON deserialization.
 * This annotation can be used on individual fields to indicate they should be validated
 * when deserializing from JSON input.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonValidInput {
    /**
     * Optional message for validation failure
     */
    String message() default "Field failed JSON input validation";
}
