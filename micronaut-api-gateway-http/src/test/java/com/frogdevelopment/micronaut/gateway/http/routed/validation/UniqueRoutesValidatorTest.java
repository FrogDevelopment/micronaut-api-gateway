package com.frogdevelopment.micronaut.gateway.http.routed.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewaySubRoute;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
class UniqueRoutesValidatorTest {

    @InjectMocks
    private UniqueRoutesValidator uniqueRoutesValidator;

    @Mock
    private AnnotationValue<UniqueRoutes> annotationMetadata;
    @Mock
    private ConstraintValidatorContext context;

    @Test
    void should_return_false_when_null() {
        // when
        var valid = uniqueRoutesValidator.isValid(null, annotationMetadata, context);

        // then
        assertThat(valid).isFalse();
        then(context).should().messageTemplate("Routes mapping can not be empty");
    }

    @Test
    void should_return_false_when_empty() {
        // given
        var mapping = new HashMap<String, GatewayRoute>();

        // when
        var valid = uniqueRoutesValidator.isValid(mapping, annotationMetadata, context);

        // then
        assertThat(valid).isFalse();
        then(context).should().messageTemplate("Routes mapping can not be empty");
    }

    @Test
    void should_return_false_when_invalid() {
        // given
        var mapping = new HashMap<String, GatewayRoute>();
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
        var valid = uniqueRoutesValidator.isValid(mapping, annotationMetadata, context);

        // then
        assertThat(valid).isFalse();
        then(context).should().messageTemplate("Route must be unique: [route_1_2] is defined for 'app_2' and 'app_1'");
    }

    @Test
    void should_return_true_when_valid() {
        // given
        var mapping = new HashMap<String, GatewayRoute>();
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
        var valid = uniqueRoutesValidator.isValid(mapping, annotationMetadata, context);

        // then
        assertThat(valid).isTrue();
        then(context).shouldHaveNoInteractions();
    }

}
