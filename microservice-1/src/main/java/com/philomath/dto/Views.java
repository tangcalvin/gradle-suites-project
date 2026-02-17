package com.philomath.dto;

/**
 * JsonView interfaces for controlling which fields are serialized
 * based on the view context, and validation groups for conditional validation.
 */
public class Views {
    /**
     * View for Endpoint 1: includes common attributes + phone + address
     */
    public interface Endpoint1 extends Default {
    }

    /**
     * View for Endpoint 2: includes common attributes + birthDate + department + salary
     */
    public interface Endpoint2 extends Default {
    }

    /**
     * Default view interface - fields marked with this are always serialized
     * in all view contexts (similar to Default validation group concept)
     */
    public interface Default {
    }

    /**
     * Validation groups for conditional validation based on endpoint
     */
    public static class ValidationGroups {
        public interface CommonValidation {
        }

        /**
         * Validation group for Endpoint 1: validates common + Endpoint1 specific fields
         */
        public interface Endpoint1Validation extends CommonValidation {
        }

        /**
         * Validation group for Endpoint 2: validates common + Endpoint2 specific fields
         */
        public interface Endpoint2Validation extends CommonValidation {
        }

        /**
         * Composed group that includes both Endpoint1 and Endpoint2 validations.
         * Useful when you want to validate fields from both endpoints simultaneously.
         * When you use @Validated(Combined.class), it will validate fields marked with
         * both Endpoint1Validation and Endpoint2Validation groups.
         */
        public interface Combined extends Endpoint1Validation, Endpoint2Validation {
        }
    }
}

