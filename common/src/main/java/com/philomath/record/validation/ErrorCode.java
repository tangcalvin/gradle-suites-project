package com.philomath.record.validation;

import jakarta.validation.Payload;

public class ErrorCode {
    public static class InvalidUsername implements Payload {
    }

    public static class InvalidEmail implements Payload {
    }

    public static class InvalidDepartment implements Payload {
    }

    public static class InvalidStockCode implements Payload {
    }

}
