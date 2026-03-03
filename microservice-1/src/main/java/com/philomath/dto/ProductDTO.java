package com.philomath.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * ProductDTO with 10 fields demonstrating constraint violations collection.
 * <p>
 * Fields with various types:
 * 1. productId (Long)
 * 2. quantity (Integer)
 * 3. createdAt (OffsetDateTime)
 * 4. manufactureDateAt (LocalDate)
 * 5. expiryTime (LocalTime)
 * 6. price (BigDecimal)
 * 7. productName (String)
 * 8. description (String)
 * 9. sku (String)
 * 10. discount (BigDecimal)
 * <p>
 * This DTO supports:
 * - @JsonValidated for enabling validation during JSON deserialization
 * - @JsonDeserialize with custom deserializer to collect ALL violations (type mismatches + constraints)
 * - Multi-field constraint violations collection via @Valid annotation on controller
 */
@JsonValidated
@JsonDeserialize(using = ProductDTODeserializer.class)
public class ProductDTO {

    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be a positive number")
    @JsonValidInput
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    private Integer quantity;

    @NotNull(message = "Created timestamp cannot be null")
    @PastOrPresent(message = "Created timestamp must be in the past or present")
    private OffsetDateTime createdAt;

    @NotNull(message = "Manufacture date cannot be null")
    @PastOrPresent(message = "Manufacture date must be in the past or present")
    private LocalDate manufactureDateAt;

    @NotNull(message = "Expiry time cannot be null")
    private LocalTime expiryTime;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than or equal to 0.01")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Price must have at most 6 integer digits and 2 fraction digits")
    private BigDecimal price;

    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String productName;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotBlank(message = "SKU cannot be blank")
    @Pattern(regexp = "^[A-Z0-9]{5,15}$", message = "SKU must be between 5 and 15 uppercase alphanumeric characters")
    private String sku;

    @NotNull(message = "Discount cannot be null")
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    @DecimalMax(value = "100.00", message = "Discount cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Discount must have at most 3 integer digits and 2 fraction digits")
    private BigDecimal discount;

    // Constructors
    public ProductDTO() {
    }

    public ProductDTO(Long productId, Integer quantity, OffsetDateTime createdAt, LocalDate manufactureDateAt,
                      LocalTime expiryTime, BigDecimal price, String productName, String description,
                      String sku, BigDecimal discount) {
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.manufactureDateAt = manufactureDateAt;
        this.expiryTime = expiryTime;
        this.price = price;
        this.productName = productName;
        this.description = description;
        this.sku = sku;
        this.discount = discount;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getManufactureDateAt() {
        return manufactureDateAt;
    }

    public void setManufactureDateAt(LocalDate manufactureDateAt) {
        this.manufactureDateAt = manufactureDateAt;
    }

    public LocalTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                ", manufactureDateAt=" + manufactureDateAt +
                ", expiryTime=" + expiryTime +
                ", price=" + price +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", discount=" + discount +
                '}';
    }
}
