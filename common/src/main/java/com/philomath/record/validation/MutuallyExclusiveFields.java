package com.philomath.record.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MutuallyExclusiveFieldsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MutuallyExclusiveFields {
    String message() default "Fields are mutually exclusive";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String firstField();

    String secondField();
}

