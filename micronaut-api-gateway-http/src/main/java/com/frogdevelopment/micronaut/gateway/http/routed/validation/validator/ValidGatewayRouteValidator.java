package com.frogdevelopment.micronaut.gateway.http.routed.validation.validator;

import java.net.URI;
import java.net.URISyntaxException;
import jakarta.inject.Singleton;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.validation.annotation.ValidGatewayRoute;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;

@Singleton
@Introspected
public class ValidGatewayRouteValidator implements ConstraintValidator<ValidGatewayRoute, GatewayRoute> {

    @Override
    public boolean isValid(final GatewayRoute gatewayRoute,
                           @NonNull final AnnotationValue<ValidGatewayRoute> annotationMetadata,
                           @NonNull final ConstraintValidatorContext context) {

        var hasViolations = false;

        if (StringUtils.isEmpty(gatewayRoute.getServiceId()) && StringUtils.isEmpty(gatewayRoute.getUri())) {
            context.buildConstraintViolationWithTemplate("At least one of fields [serviceId] or [uri] is required")
                    .addConstraintViolation();
            hasViolations = true;
        } else if (StringUtils.isNotEmpty(gatewayRoute.getServiceId()) && StringUtils.isNotEmpty(gatewayRoute.getUri())) {
            context.buildConstraintViolationWithTemplate("Only one of fields [serviceId] or [uri] has to be filled")
                    .addConstraintViolation();
            hasViolations = true;
        }

        if (StringUtils.isNotEmpty(gatewayRoute.getUri())) {
            try {
                new URI(gatewayRoute.getUri());
            } catch (URISyntaxException e) {
                context.buildConstraintViolationWithTemplate("the given URI is invalid")
                        .addPropertyNode("uri")
                        .addConstraintViolation();
                hasViolations = true;
            }
        }

        if (CollectionUtils.isEmpty(gatewayRoute.getMapping())) {
            context.buildConstraintViolationWithTemplate("must not be empty")
                    .addPropertyNode("mapping")
                    .addConstraintViolation();
            hasViolations = true;
        }

        return hasViolations;
    }
}
