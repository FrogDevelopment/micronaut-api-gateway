package com.frogdevelopment.micronaut.gateway.http.routed.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRouteProperties;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewaySubRoute;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.validation.validator.Validator;

@Tag("integrationTest")
@MicronautTest(environments = "routed")
class UniqueRoutesValidatorTest {

    @Inject
    private Validator validator;

    @Test
    void should_return_false_when_null() {
        // given
        final var gatewayRouteProperties = new GatewayRouteProperties();
        gatewayRouteProperties.setRoutes(null);

        // when
        final var constraintViolations = validator.validate(gatewayRouteProperties);

        // then
        assertThat(toMap(constraintViolations))
                .hasSize(1)
                .containsEntry("routes", "Can not be empty or null");
    }

    @Test
    void should_return_false_when_empty() {
        // given
        final var mapping = new HashMap<String, GatewayRoute>();
        final var gatewayRouteProperties = new GatewayRouteProperties();
        gatewayRouteProperties.setRoutes(mapping);

        // when
        final var constraintViolations = validator.validate(gatewayRouteProperties);

        // then
        assertThat(toMap(constraintViolations))
                .hasSize(1)
                .containsEntry("routes", "Can not be empty or null");
    }

    @Test
    void should_return_false_when_invalid() {
        // given
        final var mapping = new HashMap<String, GatewayRoute>();
        final var gatewayRouteProperties = new GatewayRouteProperties();
        gatewayRouteProperties.setRoutes(mapping);

        final var routeBuilder = GatewayRoute.builder();
        final var subRouteBuilder = GatewaySubRoute.builder();
        mapping.put("app_1", routeBuilder
                .serviceId("service_1")
                .mapping(subRouteBuilder.route("route_1_1").build())
                .mapping(subRouteBuilder.route("route_1_2").build())
                .build());
        routeBuilder.clearMapping();
        mapping.put("app_2", routeBuilder
                .serviceId("service_2")
                .mapping(subRouteBuilder.route("route_2_1").build())
                .mapping(subRouteBuilder.route("route_1_2").build())
                .build());

        // when
        final var constraintViolations = validator.validate(gatewayRouteProperties);

        // then
        assertThat(toMap(constraintViolations))
                .hasSize(1)
                .containsEntry("routes", "Route must be unique: [route_1_2] is defined for 'app_2' and 'app_1'");
    }

    @Test
    void should_return_true_when_valid() {
        // given
        final var mapping = new HashMap<String, GatewayRoute>();
        final var gatewayRouteProperties = new GatewayRouteProperties();
        gatewayRouteProperties.setRoutes(mapping);

        final var routeBuilder = GatewayRoute.builder();
        final var subRouteBuilder = GatewaySubRoute.builder();
        mapping.put("app_1", routeBuilder
                .serviceId("service_1")
                .mapping(subRouteBuilder.route("route_1_1").build())
                .mapping(subRouteBuilder.route("route_1_2").build())
                .build());
        routeBuilder.clearMapping();
        mapping.put("app_2", routeBuilder
                .serviceId("service_2")
                .mapping(subRouteBuilder.route("route_2_1").build())
                .mapping(subRouteBuilder.route("route_2_2").build())
                .build());

        // when
        final var constraintViolations = validator.validate(gatewayRouteProperties);

        // then
        assertThat(toMap(constraintViolations)).isEmpty();
    }

    private static <T> Map<String, String> toMap(Set<ConstraintViolation<T>> constraintViolations) {
        return constraintViolations.stream()
                .map(constraintViolation -> {
                    final var path = constraintViolation.getPropertyPath().toString();
                    final var message = constraintViolation.getMessage();
                    return new AbstractMap.SimpleEntry<>(path, message);
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
