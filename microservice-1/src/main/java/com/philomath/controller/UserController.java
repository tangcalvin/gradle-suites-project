package com.philomath.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.philomath.dto.User;
import com.philomath.dto.Views;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for User management with JsonView demonstration.
 * <p>
 * Two endpoints returning different subsets of User attributes:
 * - Endpoint 1 (/users/endpoint1): Validates and returns common attributes + phone + address
 * - Endpoint 2 (/users/endpoint2): Validates and returns common attributes + birthDate + department + salary
 */
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    /**
     * Endpoint 1: POST endpoint that accepts and returns user with common attributes + contact information
     * <p>
     * Validates with Endpoint1Validation group:
     * - Validates: id, username, email, firstName, lastName, phone, address
     * - Ignores: birthDate, department, salary
     * <p>
     * Serializes with Endpoint1 view:
     * - Returns: id, username, email, firstName, lastName, phone, address
     *
     * @param user User object with Endpoint1 attributes
     * @return User object with Endpoint1 view
     */
    @PostMapping("/endpoint1")
    @JsonView(Views.Endpoint1.class)
    public ResponseEntity<User> createUserEndpoint1(
            @Validated(Views.ValidationGroups.Endpoint1Validation.class)
            @RequestBody @JsonView(Views.Endpoint1.class) User user) {
        // Process user with Endpoint1 view (phone, address)
        // In a real application, this would save to database, validate, etc.

        return ResponseEntity.ok(user);
    }

    /**
     * Endpoint 2: POST endpoint that accepts and returns user with common attributes + employment information
     * <p>
     * Validates with Endpoint2Validation group:
     * - Validates: id, username, email, firstName, lastName, birthDate, department, salary
     * - Ignores: phone, address
     * <p>
     * Serializes with Endpoint2 view:
     * - Returns: id, username, email, firstName, lastName, birthDate, department, salary
     *
     * @param user User object with Endpoint2 attributes
     * @return User object with Endpoint2 view
     */
    @PostMapping("/endpoint2")
    @JsonView(Views.Endpoint2.class)
    public ResponseEntity<User> createUserEndpoint2(
            @Validated(Views.ValidationGroups.Endpoint2Validation.class)
            @RequestBody @JsonView(Views.Endpoint2.class) User user) {
        // Process user with Endpoint2 view (birthDate, department, salary)
        // In a real application, this would save to database, validate, etc.

        return ResponseEntity.ok(user);
    }
}
