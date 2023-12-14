package com.frogdevelopment.micronaut.gateway.http.routed.validation.validator;

import java.util.HashMap;
import java.util.Map;
import jakarta.inject.Singleton;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.validation.annotation.UniqueRoutes;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;

@Singleton
@Introspected
public class UniqueRoutesValidator implements ConstraintValidator<UniqueRoutes, Map<String, GatewayRoute>> {

    private static final String ERROR_MSG = "Route must be unique: [%s] is defined for '%s' and '%s'";

    @Override
    public boolean isValid(final Map<String, GatewayRoute> mapping,
            @NonNull final AnnotationValue<UniqueRoutes> annotationMetadata,
            @NonNull final ConstraintValidatorContext context) {

        if (CollectionUtils.isEmpty(mapping)) {
            context.messageTemplate("Can not be empty or null");
            return false;
        }

        final var serviceByRoute = new HashMap<String, String>();
        for (final var entry : mapping.entrySet()) {
            final var mappingName = entry.getKey();
            final var gatewayRoute = entry.getValue();
            for (final var subRoute : gatewayRoute.getMapping()) {
                final var previousValue = serviceByRoute.putIfAbsent(subRoute.getRoute(), mappingName);
                if (previousValue != null) {
                    context.messageTemplate(ERROR_MSG.formatted(subRoute.getRoute(), mappingName, previousValue));
                    return false;
                }
            }
        }

        return true;
    }
}
