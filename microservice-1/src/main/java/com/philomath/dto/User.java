package com.philomath.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

import java.math.BigDecimal;

/**
 * User DTO with 10 attributes demonstrating JsonView functionality.
 * <p>
 * Common attributes (1-5): id, username, email, firstName, lastName
 * Endpoint 1 specific (6-7): phone, address
 * Endpoint 2 specific (8-10): birthDate, department, salary
 */
public class User {

    // Common attributes (shown in both endpoints)
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be a positive number")
    @JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
    private String email;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @JsonView({Views.Endpoint1.class, Views.Endpoint2.class})
    private String lastName;

    // Endpoint 1 specific attributes
    @NotBlank(message = "Phone number cannot be blank", groups = Views.ValidationGroups.Endpoint1Validation.class)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be a valid E.164 format", groups = Views.ValidationGroups.Endpoint1Validation.class)
    @JsonView(Views.Endpoint1.class)
    private String phone;

    @Valid  // ← Enables cascading validation of nested Address object
//    @ConvertGroup(from = Views.ValidationGroups.Endpoint1Validation.class, to = Default.class)
//    @ConvertGroup(from = Views.ValidationGroups.Endpoint2Validation.class, to = Default.class)
    @ConvertGroup(from = Views.ValidationGroups.CommonValidation.class, to = Default.class)
    // ← Map Endpoint1Validation to Default group for nested validation
    @NotNull(message = "Address cannot be null", groups = {Views.ValidationGroups.Endpoint1Validation.class})
    @JsonView({Views.Endpoint1.class})
    // ← Address is included in both views for demonstration
    private Address address;

    // Endpoint 2 specific attributes
    @NotBlank(message = "Birth date cannot be blank", groups = Views.ValidationGroups.Endpoint2Validation.class)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Birth date must be in yyyy-MM-dd format", groups = Views.ValidationGroups.Endpoint2Validation.class)
    @JsonView(Views.Endpoint2.class)
    private String birthDate;

    @NotBlank(message = "Department cannot be blank", groups = Views.ValidationGroups.Endpoint2Validation.class)
    @Size(min = 2, max = 50, message = "Department must be between 2 and 50 characters", groups = Views.ValidationGroups.Endpoint2Validation.class)
    @JsonView(Views.Endpoint2.class)
    private String department;

    @NotNull(message = "Salary cannot be null", groups = Views.ValidationGroups.Endpoint2Validation.class)
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0", groups = Views.ValidationGroups.Endpoint2Validation.class)
    @Digits(integer = 10, fraction = 2, message = "Salary must have at most 10 integer digits and 2 fraction digits", groups = Views.ValidationGroups.Endpoint2Validation.class)
    @JsonView(Views.Endpoint2.class)
    private BigDecimal salary;

    // Constructors
    public User() {
    }

    public User(Long id, String username, String email, String firstName, String lastName,
                String phone, Address address, String birthDate, String department, BigDecimal salary) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.birthDate = birthDate;
        this.department = department;
        this.salary = salary;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                '}';
    }
}
