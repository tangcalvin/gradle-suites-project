package com.philomath.record.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
//@Pattern(regexp = "")  // placeholder, will be dynamically set or documented
public @interface ValidEnum {

    Class<? extends Enum<?>> enumClass();

    String message() default "must be any of the enum {enumClass}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
