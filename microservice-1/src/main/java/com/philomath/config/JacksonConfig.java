package com.philomath.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
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

            builder.withCoercionConfig(LogicalType.Textual, config -> {
                config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Array, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Object, CoercionAction.Fail);
            });

            builder.withCoercionConfig(LogicalType.Integer, config -> {
                config.setCoercion(CoercionInputShape.String, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
            });

            builder.withCoercionConfig(LogicalType.Float, config -> {
                config.setCoercion(CoercionInputShape.String, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
            });

            builder.withCoercionConfig(LogicalType.Boolean, config -> {
                config.setCoercion(CoercionInputShape.String, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
            });

            builder.withCoercionConfig(LogicalType.Array, config -> {
                config.setCoercion(CoercionInputShape.String, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
            });

            builder.withCoercionConfig(LogicalType.POJO, config -> {
                config.setCoercion(CoercionInputShape.String, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
            });

            builder.withCoercionConfig(LogicalType.DateTime, config -> {
                config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
            });
        };
    }
}
