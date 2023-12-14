package com.frogdevelopment.micronaut.gateway.http.routed.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;

import com.frogdevelopment.micronaut.gateway.http.routed.validation.validator.UniqueRoutesValidator;

@Documented
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueRoutesValidator.class)
public @interface UniqueRoutes {

}
