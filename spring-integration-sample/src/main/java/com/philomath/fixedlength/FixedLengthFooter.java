package com.philomath.fixedlength;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import name.velikodniy.vitaliy.fixedlength.Align;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

/**
 * Fixed-length file footer: F + recordCount(10) + totalAmount(20).
 * Total 31 chars.
 */
@Data
@FixedLine(startsWith = "F")
public class FixedLengthFooter {

    @FixedField(offset = 2, length = 10, align = Align.RIGHT, padding = '0')
    @NotNull(message = "recordCount must not be null")
    public Integer recordCount;

    @FixedField(offset = 12, length = 20, align = Align.RIGHT, padding = '0')
    @Pattern(regexp = "^\\d{1,20}$", message = "totalAmount must be numeric")
    public String totalAmount;
}
