package com.philomath.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.philomath.record.validation.ErrorCode;
import com.philomath.record.validation.ValidEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class Staff {
    // Same as User even validation rules
    @NotEmpty(message = "Username must not be empty", payload = ErrorCode.InvalidUsername.class)
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters", payload = ErrorCode.InvalidUsername.class)
    String username;

    @Email(message = "Email should be valid", payload = ErrorCode.InvalidEmail.class)
    String email;

    @ValidEnum(enumClass = Gender.class, message = "Gender must be one of the defined values", payload = ErrorCode.InvalidUsername.class)
//    @Pattern(regexp = "^(M|F)$", message = "Gender must be 'M' or 'F'", payload = ErrorCode.InvalidUsername.class)
    String gender;

    Staff(String username, String email) {
        this.username = username;
        this.email = email;
        // TODO: How to do validation seamlessly?
//        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
//        Set<ConstraintViolation<Staff>> violations = validator.validate(this);
//        if (!violations.isEmpty()) {
//            throw new ConstraintViolationException(violations);
//        }
    }

    @Getter
    @AllArgsConstructor
    public enum Gender {
        M("Male"),
        F("Female");
        private String name;

        @JsonCreator
        public static Gender fromString(String value) {
            for (Gender role : Gender.values()) {
                if (role.name().equalsIgnoreCase(value)) {
                    return role;
                }
            }
            return null; // or a default Role
        }

        @JsonValue
        public String toValue() {
            return name();
        }
    }
}
