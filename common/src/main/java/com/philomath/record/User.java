package com.philomath.record;

import com.philomath.record.validation.ErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record User(
        @NotEmpty(message = "Username must not be empty", payload = ErrorCode.InvalidUsername.class)
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters", payload = ErrorCode.InvalidUsername.class)
        String username,
        @Email(message = "Email should be valid", payload = ErrorCode.InvalidEmail.class)
        String email) {

//    // Custom constructor to perform validation
//    public User {
//        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
//        Set<ConstraintViolation<User>> violations = validator.validate(this);
//        if (!violations.isEmpty()) {
//            throw new ConstraintViolationException(violations);
//        }
//    }
}