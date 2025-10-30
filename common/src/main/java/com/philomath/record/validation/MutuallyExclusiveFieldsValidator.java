package com.philomath.record.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;

public class MutuallyExclusiveFieldsValidator implements ConstraintValidator<MutuallyExclusiveFields, Object> {

    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(MutuallyExclusiveFields constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.firstField();
        this.secondFieldName = constraintAnnotation.secondField();
    }

    @SneakyThrows
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object firstValue = BeanUtils.getProperty(value, firstFieldName);
        Object secondValue = BeanUtils.getProperty(value, secondFieldName);

        // Example condition: must be mutually exclusive
        if ((firstValue == null) == (secondValue == null)) {
            context.disableDefaultConstraintViolation();

            // Set violation path to firstField
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(firstFieldName + " / " + secondFieldName)
                    .addConstraintViolation();

            // Optionally add violation path for secondField if needed
//            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
//                    .addPropertyNode(secondFieldName)
//                    .addConstraintViolation();

            return false; // invalid
        }
        return true; // valid
    }
}
