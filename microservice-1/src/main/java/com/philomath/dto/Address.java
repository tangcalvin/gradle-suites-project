package com.philomath.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

/**
 * Address DTO demonstrating nested object validation with @Validated.
 * This is a cascading validation example - the Address object itself is validated
 * when the User object is validated.
 */
public class Address {

    @NotBlank(message = "Street cannot be blank", groups = Default.class)
    @Size(min = 5, max = 100, message = "Street must be between 5 and 100 characters", groups = Default.class)
    @JsonView({Views.Default.class})
    private String street;

    @NotBlank(message = "City cannot be blank", groups = Default.class)
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters", groups = Default.class)
    @JsonView({Views.Default.class})
    private String city;

    @NotBlank(message = "State/Province cannot be blank", groups = Default.class)
    @Size(min = 2, max = 50, message = "State/Province must be between 2 and 50 characters", groups = Default.class)
    @JsonView({Views.Default.class})
    private String state;

    @NotBlank(message = "Postal code cannot be blank", groups = Default.class)
    @Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Postal code must be 3-10 alphanumeric characters (uppercase)", groups = Default.class)
    @JsonView({Views.Default.class})
    private String postalCode;

    @NotBlank(message = "Country cannot be blank", groups = Default.class)
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters", groups = Default.class)
    @JsonView({Views.Default.class})
    private String country;

    // Constructors
    public Address() {
    }

    public Address(String street, String city, String state, String postalCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    // Getters and Setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
