package com.philomath.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom deserializer for Long fields that captures type mismatch errors
 */
public class LongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken().isNumeric()) {
            return p.getLongValue();
        }

        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }

        // This will be caught by the custom deserializer or validator
        throw new IllegalArgumentException("Cannot deserialize value of type `java.lang.Long` from String \"" + value + "\": not a valid `java.lang.Long` value");
    }
}
