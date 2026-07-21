package com.philomath.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.LogicalType;

/**
 * Strict JSON typing: each field accepts only its declared Java type.
 * <p>
 * Spring Boot 4 uses Jackson 3 ({@code tools.jackson}); configured via {@link JsonMapperBuilderCustomizer}.
 * Applies globally, including {@link com.philomath.dto.TenFieldDTO} on {@code POST /api/sample}.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer strictTypeCoercionCustomizer() {
        return builder -> {
            builder.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
            builder.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
            builder.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

            applyStrictCoercion(builder);
        };
    }

    /**
     * Fail every cross-type coercion by iterating {@link LogicalType} and rejecting
     * all {@link CoercionInputShape}s that are not native for that type.
     */
    private static void applyStrictCoercion(JsonMapper.Builder builder) {
        for (LogicalType targetType : LogicalType.values()) {
            if (targetType == LogicalType.Untyped) {
                continue; // Object / JsonNode — accepts any JSON shape
            }
            builder.withCoercionConfig(targetType, config -> {
                for (CoercionInputShape inputShape : CoercionInputShape.values()) {
                    if (!isNativeInput(targetType, inputShape)) {
                        config.setCoercion(inputShape, CoercionAction.Fail);
                    }
                }
            });
        }
    }

    /**
     * JSON token shapes that are valid input for each logical Java type.
     * Everything else is rejected (e.g. number into String, string into int).
     */
    private static boolean isNativeInput(LogicalType targetType, CoercionInputShape inputShape) {
        return switch (targetType) {
            case Textual -> inputShape == CoercionInputShape.String;
            case Integer -> inputShape == CoercionInputShape.Integer;
            // JSON integer tokens are valid for floating-point fields (1 → 1.0)
            case Float -> inputShape == CoercionInputShape.Integer
                    || inputShape == CoercionInputShape.Float;
            case Boolean -> inputShape == CoercionInputShape.Boolean;
            case Enum -> inputShape == CoercionInputShape.String;
            case Array, Collection -> inputShape == CoercionInputShape.Array;
            case Map, POJO -> inputShape == CoercionInputShape.Object;
            case DateTime -> inputShape == CoercionInputShape.String;
            case Binary -> inputShape == CoercionInputShape.String
                    || inputShape == CoercionInputShape.Binary;
            case OtherScalar -> inputShape == CoercionInputShape.String;
            case Untyped -> true;
        };
    }
}
