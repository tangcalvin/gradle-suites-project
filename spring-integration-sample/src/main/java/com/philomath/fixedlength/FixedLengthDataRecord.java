package com.philomath.fixedlength;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import name.velikodniy.vitaliy.fixedlength.Align;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

/**
 * Fixed-length data record: D + id(10) + name(30) + amount(12).
 * Total 53 chars.
 */
@Data
@FixedLine(startsWith = "D")
public class FixedLengthDataRecord {

    @FixedField(offset = 2, length = 10, align = Align.LEFT)
    @Pattern(regexp = "^[A-Za-z0-9\\s]{1,10}$", message = "id must be alphanumeric")
    public String id;

    @FixedField(offset = 12, length = 30, align = Align.LEFT)
    @Pattern(regexp = "^[\\p{Print}\\s]{0,30}$", message = "name must be printable characters")
    public String name;

    @FixedField(offset = 42, length = 12, align = Align.RIGHT, padding = '0')
    @Pattern(regexp = "^\\d{1,12}$", message = "amount must be numeric")
    public String amount;
}
