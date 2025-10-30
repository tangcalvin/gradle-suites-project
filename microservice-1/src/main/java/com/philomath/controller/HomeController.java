package com.philomath.controller;

import com.philomath.record.Department;
import com.philomath.record.Staff;
import com.philomath.record.User;
import com.philomath.record.validation.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/")
public class HomeController {

    @Autowired
    private Validator validator;

    @PostMapping("/departments")
    public Department create(
            @Valid
            @NotNull(message = "Department must not be null", payload = ErrorCode.InvalidDepartment.class)
            @RequestBody Department department) {
        return department;
    }

    @PostMapping(value = "/users", produces = "application/json")
    public Staff create(
            @Valid
            @RequestBody Staff staff) {
//        Set<ConstraintViolation<User>> violations = validator.validate(user);
//        if (!violations.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (ConstraintViolation<User> violation : violations) {
//                sb.append(violation.getMessage()).append("; ");
//            }
//            throw new IllegalArgumentException("Validation failed: " + sb);
//        }

//        if (!violations.isEmpty()) {
//            throw new ConstraintViolationException(violations);
//        }
        return staff;
    }

    @GetMapping("/stocks/{stockCode}")
    public Map<String, Object> getStockInfo(
            @Valid
            @Pattern(regexp = "^[0-9]{1,5}$", message = "Stock code must be 1 to 5 uppercase letters", payload = ErrorCode.InvalidStockCode.class)
            @PathVariable("stockCode") String stockCode,
            @Valid
            @NotEmpty(message = "ISIN must not be empty", payload = ErrorCode.InvalidStockCode.class)
            @Pattern(regexp = "^[A-Z]{2}[A-Z0-9]{10}$", message = "ISIN must be 12 characters: 2 letters followed by 10 alphanumeric characters", payload = ErrorCode.InvalidStockCode.class)
            @RequestParam String isin) {
        return Map.of(
                "success", true,
                "stockCode", stockCode,
                "price", 123.45
        );
    }

    @GetMapping("/{name}")
    public Map<String, Object> index(
            // Add validation here to path variable 'name' to ensure it is not empty and has a length between 3 and 20 characters
            @PathVariable("name") String name) {
        User user = new User(name, "abc@gmail.com");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        return Map.of(
                "success", true,
                "message", "Welcome to Philomath Microservice 1",
                "user", user
        );
    }
}
