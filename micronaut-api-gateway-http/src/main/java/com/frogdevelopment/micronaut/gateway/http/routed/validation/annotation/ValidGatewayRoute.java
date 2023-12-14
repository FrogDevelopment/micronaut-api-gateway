package com.frogdevelopment.micronaut.gateway.http.routed.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;

import com.frogdevelopment.micronaut.gateway.http.routed.validation.validator.ValidGatewayRouteValidator;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidGatewayRouteValidator.class)
public @interface ValidGatewayRoute {

}
