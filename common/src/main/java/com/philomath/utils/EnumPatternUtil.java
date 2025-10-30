package com.philomath.utils;

// Utility class to generate the pattern for OpenAPI
public class EnumPatternUtil {

    public static String patternFor(Class<? extends Enum<?>> enumClass) {
        StringBuilder sb = new StringBuilder();
        sb.append("^(");
        Enum<?>[] enums = enumClass.getEnumConstants();
        for (int i = 0; i < enums.length; i++) {
            sb.append(enums[i].name());
            if (i < enums.length - 1) {
                sb.append("|");
            }
        }
        sb.append(")$");
        return sb.toString();
    }
}

