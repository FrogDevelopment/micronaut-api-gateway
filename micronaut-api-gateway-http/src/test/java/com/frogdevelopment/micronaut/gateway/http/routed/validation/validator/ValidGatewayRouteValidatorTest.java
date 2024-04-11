package com.frogdevelopment.micronaut.gateway.http.routed.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewaySubRoute;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.validation.validator.Validator;

@Tag("integrationTest")
@MicronautTest(environments = "routed")
class ValidGatewayRouteValidatorTest {

    @Inject
    private Validator validator;

    @Test
    void should_return_constraintViolations_when_empty() {
        // given
        final var gatewayRoute = GatewayRoute.builder().build();

        // when
        final var constraintViolations = validator.validate(gatewayRoute);

        // then
        assertThat(toMap(constraintViolations))
                .hasSize(2)
                .containsEntry("", "At least one of fields [serviceId] or [uri] is required")
                .containsEntry("mapping", "must not be empty");
    }

    @Test
    void should_return_constraintViolations_when_both_serviceId_and_uri_areSet() {
        // given
        final var gatewayRoute = GatewayRoute.builder()
                .serviceId("my_service_id")
                .uri("my_uri")
                .mapping(GatewaySubRoute.builder().build())
                .build();

        // when
        final var constraintViolations = validator.validate(gatewayRoute);

        // then
        assertThat(toMap(constraintViolations))
                .hasSize(1)
                .containsEntry("", "Only one of fields [serviceId] or [uri] has to be filled");
    }

    @Test
    void should_return_constraintViolations_when_uri_invalid() {
        // given
        final var gatewayRoute = GatewayRoute.builder()
                .uri("$^ my_uri")
                .mapping(GatewaySubRoute.builder().build())
                .build();

        // when
        final var constraintViolations = validator.validate(gatewayRoute);

        // then
        assertThat(toMap(constraintViolations))
                .hasSize(1)
                .containsEntry("uri", "the given URI is invalid");
    }

    @Test
    void should_return_emptyConstraintViolations_when_valid() {
        // given
        final var gatewayRoute = GatewayRoute.builder()
                .serviceId("my-service")
                .mapping(GatewaySubRoute.builder().build())
                .build();

        // when
        final var constraintViolations = validator.validate(gatewayRoute);

        // then
        assertThat(toMap(constraintViolations)).isEmpty();
    }

    private static <T> Map<String, String> toMap(Set<ConstraintViolation<T>> constraintViolations) {
        return constraintViolations.stream()
                .collect(Collectors.toMap(
                        constraintViolation -> constraintViolation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage));
    }

}
