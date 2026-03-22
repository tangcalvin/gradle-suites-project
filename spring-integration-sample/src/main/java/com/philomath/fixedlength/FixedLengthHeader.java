package com.philomath.fixedlength;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import name.velikodniy.vitaliy.fixedlength.Align;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

/**
 * Fixed-length file header: H + fileId(10) + timestamp(14) + recordCount(8).
 * Total 33 chars.
 */
@Data
@FixedLine(startsWith = "H")
public class FixedLengthHeader {

    @FixedField(offset = 2, length = 10, align = Align.LEFT)
    @Pattern(regexp = "^[A-Za-z0-9\\s]{1,10}$", message = "fileId must be alphanumeric")
    public String fileId;

    @FixedField(offset = 12, length = 14, align = Align.LEFT)
    @Pattern(regexp = "^\\d{14}$", message = "timestamp must be 14 digits (YYYYMMDDHHmmss)")
    public String timestamp;

    @FixedField(offset = 26, length = 8, align = Align.RIGHT, padding = '0')
    @NotNull(message = "recordCount must not be null")
    public Integer recordCount;
}
