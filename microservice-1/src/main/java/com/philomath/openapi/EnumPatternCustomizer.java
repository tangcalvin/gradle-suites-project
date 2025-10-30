package com.philomath.openapi;

import com.philomath.utils.EnumPatternUtil;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EnumPatternCustomizer implements PropertyCustomizer {
    @Override
    public Schema customize(Schema property, AnnotatedType type) {
        // Try to get the raw class in different ways
        Class<?> clazz = null;

        // First, try getType()
        Object rawType = type.getType();
        if (rawType instanceof Class<?>) {
            clazz = (Class<?>) rawType;
        }

        // If still null, try context annotations to detect @ValidEnum or other hints
        if (clazz == null && type.getCtxAnnotations() != null) {
            for (Annotation ann : type.getCtxAnnotations()) {
                if (ann.annotationType().getSimpleName().equals("ValidEnum")) {
                    try {
                        // reflectively get enumClass() value from annotation instance
                        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) ann.annotationType()
                                .getMethod("enumClass").invoke(ann);
                        if (enumClass != null) {
//                            String pattern = EnumPatternUtil.patternFor(enumClass);
//                            property.setPattern(pattern);
                            List<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                                    .map(Enum::name)
                                    .collect(Collectors.toList());
                            property.setEnum(enumNames);
                        }
                    } catch (Exception e) {
                        // log or ignore
                    }
                    break;
                }
            }
        }

        // If clazz still set, handle enum pattern here if applicable
        else if (clazz != null && clazz.isEnum()) {
            String pattern = EnumPatternUtil.patternFor((Class<? extends Enum<?>>) clazz);
            property.setPattern(pattern);
        }

        return property;
    }
}
