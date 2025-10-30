package com.philomath.record;

import com.philomath.record.validation.ErrorCode;
import com.philomath.record.validation.MutuallyExclusiveFields;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

/**
 * Department should have fields
 * 1. code - String[4]
 * 2. name - String[50]
 * 3. description - String[200]
 * 4. Set of Staff
 */
@Getter
@MutuallyExclusiveFields(firstField = "isin", secondField = "stockCode",
        message = "Either ISIN or Stock Code must be provided, but not both",
        payload = ErrorCode.InvalidDepartment.class)
public class Department {
    @NotEmpty(message = "Department code must not be empty", payload = ErrorCode.InvalidDepartment.class)
    @Size(min = 4, max = 4, message = "Department code must be exactly 4 characters", payload = ErrorCode.InvalidDepartment.class)
    String code;

    @NotEmpty(message = "Department name must not be empty", payload = ErrorCode.InvalidDepartment.class)
    @Size(min = 3, max = 50, message = "Department name must be between 3 and 50 characters", payload = ErrorCode.InvalidDepartment.class)
    String name;

    @NotEmpty(message = "Department description must not be empty", payload = ErrorCode.InvalidDepartment.class)
    String description;

    @Size(min = 12, max = 12, message = "ISIN must be exactly 12 characters", payload = ErrorCode.InvalidDepartment.class)
    String isin;

    @Size(min = 1, max = 5, message = "Stock code must be between 1 and 5 characters", payload = ErrorCode.InvalidDepartment.class)
    String stockCode;

    @Valid
    List<Staff> staffs;

    Department(String code, String name, String description, List<Staff> staffs, String isin, String stockCode) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.isin = isin;
        this.stockCode = stockCode;
        this.staffs = staffs;
        // TODO: How to do validation seamlessly?
//        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
//        Set<ConstraintViolation<Department>> violations = validator.validate(this);
//        if (!violations.isEmpty()) {
//            throw new ConstraintViolationException(violations);
//        }
    }
}
