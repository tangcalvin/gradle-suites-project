package com.philomath.dto;

import java.lang.annotation.*;

/**
 * Custom @JsonValidated annotation to enable validation during JSON deserialization.
 * This annotation marks a class to be validated when deserialized from JSON.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonValidated {
    /**
     * Optional value to specify validation groups to use.
     */
    Class<?>[] value() default {};
}
