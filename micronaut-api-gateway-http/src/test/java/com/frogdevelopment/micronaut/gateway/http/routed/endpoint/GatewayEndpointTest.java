package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import static io.micronaut.health.HealthStatus.UNKNOWN;
import static io.micronaut.health.HealthStatus.UP;
import static io.micronaut.http.HttpStatus.OK;
import static io.micronaut.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.mockito.BDDMockito.given;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRouteProperties;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewaySubRoute;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GatewayEndpointTest {

    private final GatewaySubRoute gatewaySubRoute1 = GatewaySubRoute.builder()
            .route("route1")
            .build();
    private final GatewayRoute gatewayRoute1 = GatewayRoute.builder()
            .serviceId("service_id")
            .mapping(gatewaySubRoute1)
            .build();
    private final GatewaySubRoute gatewaySubRoute2 = GatewaySubRoute.builder()
            .route("route2")
            .build();
    private final GatewayRoute gatewayRoute2 = GatewayRoute.builder()
            .uri("/my_uri")
            .mapping(gatewaySubRoute2)
            .build();

    @InjectMocks
    private GatewayEndpoint gatewayEndpoint;

    @Mock
    private GatewayRouteProperties gatewayRouteProperties;
    @Mock
    private UriHealthCheck uriHealthCheck;
    @Mock
    private ServiceHealthCheck serviceHealthCheck;

    @Test
    void should_return_OK_when_allRoutesAreHealthy() {
        // given
        given(gatewayRouteProperties.getRoutes()).willReturn(Map.of("name1", gatewayRoute1, "name2", gatewayRoute2));
        final var serviceHealth = ServiceHealth.fromService(gatewayRoute1, UP);
        given(serviceHealthCheck.checkHealth(gatewayRoute1))
                .willReturn(Mono.just(serviceHealth));
        final var uriHealth = ServiceHealth.fromUri(gatewayRoute2, UP);
        given(uriHealthCheck.checkHealth(gatewayRoute2))
                .willReturn(Mono.just(uriHealth));

        // when
        var response = gatewayEndpoint.routesHealthStatus().blockOptional();

        // then
        assertThat(response).hasValueSatisfying(value -> {
            assertThatObject(value.getStatus()).isEqualTo(OK);
            assertThat(value.getBody()).hasValueSatisfying(body -> assertThat(body)
                    .containsEntry("name1", serviceHealth)
                    .containsEntry("name2", uriHealth));
        });
    }

    @Test
    void should_return_SERVICE_UNAVAILABLE_when_atLeast1RouteIsNotHealthy() {
        // given
        given(gatewayRouteProperties.getRoutes()).willReturn(Map.of("name1", gatewayRoute1, "name2", gatewayRoute2));
        final var serviceHealth = ServiceHealth.fromService(gatewayRoute1, UP);
        given(serviceHealthCheck.checkHealth(gatewayRoute1))
                .willReturn(Mono.just(serviceHealth));
        final var uriHealth = ServiceHealth.fromUri(gatewayRoute2, UNKNOWN);
        given(uriHealthCheck.checkHealth(gatewayRoute2))
                .willReturn(Mono.just(uriHealth));

        // when
        var response = gatewayEndpoint.routesHealthStatus().blockOptional();

        // then
        assertThat(response).hasValueSatisfying(value -> {
            assertThatObject(value.getStatus()).isEqualTo(SERVICE_UNAVAILABLE);
            assertThat(value.getBody()).hasValueSatisfying(body -> assertThat(body)
                    .containsEntry("name1", serviceHealth)
                    .containsEntry("name2", uriHealth));
        });
    }

    @Test
    void should_return_selectedRouteHealth() {
        // given
        given(gatewayRouteProperties.getRoutes()).willReturn(Map.of("name1", gatewayRoute1, "name2", gatewayRoute2));
        final var serviceHealth = ServiceHealth.fromService(gatewayRoute1, UP);
        given(serviceHealthCheck.checkHealth(gatewayRoute1))
                .willReturn(Mono.just(serviceHealth));

        // when
        var response = gatewayEndpoint.routeHealthStatus("name1").blockOptional();

        // then
        assertThat(response).hasValueSatisfying(value -> {
            assertThatObject(value.getStatus()).isEqualTo(OK);
            assertThat(value.getBody()).hasValueSatisfying(body -> assertThat(body)
                    .isEqualTo(serviceHealth));
        });
    }

}
