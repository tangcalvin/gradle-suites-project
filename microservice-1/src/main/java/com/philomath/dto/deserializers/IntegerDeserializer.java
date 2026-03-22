package com.philomath.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom deserializer for Integer fields that captures type mismatch errors
 */
public class IntegerDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken().isNumeric()) {
            return p.getIntValue();
        }

        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }

        throw new IllegalArgumentException("Cannot deserialize value of type `java.lang.Integer` from String \"" + value + "\": not a valid `java.lang.Integer` value");
    }
}
