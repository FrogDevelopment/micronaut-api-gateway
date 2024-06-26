package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import static io.micronaut.http.HttpStatus.OK;
import static io.micronaut.http.HttpStatus.SERVICE_UNAVAILABLE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jakarta.inject.Singleton;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRouteProperties;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.management.endpoint.annotation.Selector;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Endpoint that will display the service health for each configured routes. If all the service are healthy, then this endpoint will return
 * {@link HttpStatus#OK} otherwise {@link HttpStatus#SERVICE_UNAVAILABLE}.
 */
@Slf4j
@Singleton
@Endpoint(id = "gateway", defaultSensitive = false)
@RequiredArgsConstructor
public final class GatewayEndpoint {

    private final GatewayRouteProperties gatewayRouteProperties;
    private final UriHealthCheck uriHealthCheck;
    private final ServiceHealthCheck serviceHealthCheck;

    @Read
    public Mono<MutableHttpResponse<Map<String, ServiceHealth>>> routesHealthStatus() {
        return Flux.fromIterable(gatewayRouteProperties.getRoutes().entrySet())
                .flatMap(entry -> toRouteHealth(entry.getKey(), entry.getValue()))
                .collectList()
                .map(result ->  {
                    final var mapHealthStatus = result.stream()
                            .collect(Collectors.toMap(RouteHealth::name, RouteHealth::serviceHealth));
                    final var allHealthy = mapHealthStatus.values()
                            .stream()
                            .allMatch(ServiceHealth::isHealthy);

                    return HttpResponse.status(allHealthy ? OK : SERVICE_UNAVAILABLE).body(new TreeMap<>(mapHealthStatus));
                });
    }

    @Read
    public Mono<MutableHttpResponse<ServiceHealth>> routeHealthStatus(@Selector final String route) {
        return Mono.justOrEmpty(gatewayRouteProperties.getRoutes().get(route))
                .flatMap(gatewayRoute -> toRouteHealth(route, gatewayRoute))
                .map(RouteHealth::serviceHealth)
                .map(serviceHealth -> HttpResponse.status(serviceHealth.isHealthy() ? OK : SERVICE_UNAVAILABLE)
                        .body(serviceHealth))
                .defaultIfEmpty(HttpResponse.notFound());
    }

    private Mono<RouteHealth> toRouteHealth(final String route, final GatewayRoute gatewayRoute) {
        return checkHealth(gatewayRoute)
                .map(serviceHealth -> new RouteHealth(route, serviceHealth));
    }

    private Mono<ServiceHealth> checkHealth(final GatewayRoute gatewayRoute) {
        if (gatewayRoute.getUri() != null) {
            return uriHealthCheck.checkHealth(gatewayRoute);
        } else {
            return serviceHealthCheck.checkHealth(gatewayRoute);
        }
    }
}
