package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UNKNOWN;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Optional;
import jakarta.inject.Singleton;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;

import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import io.micronaut.serde.annotation.Serdeable;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
@RequiredArgsConstructor
class UriHealthCheck {

    static final String DEFAULT_HEALTH = "/health";

    private final AsyncLoadingCache<String, URI> uriCache;
    private final ReactorHttpClient httpClient;

    Mono<ServiceHealth> checkHealth(final GatewayRoute gatewayRoute) {
        return Mono.fromFuture(uriCache.get(gatewayRoute.getUri()))
                .flatMap(uri -> getHealthStatus(gatewayRoute, uri))
                .onErrorReturn(DOWN)
                .map(healthStatus -> ServiceHealth.fromUri(gatewayRoute, healthStatus));
    }

    private Mono<HealthStatus> getHealthStatus(GatewayRoute gatewayRoute, URI uri) {
        var healhtUri = uri.resolve(getUriHealthEndpoint(gatewayRoute));
        return httpClient.exchange(healhtUri.toString(), HealthCheck.class)
                .map(UriHealthCheck::getHealthStatus);
    }

    private static String getUriHealthEndpoint(GatewayRoute gatewayRoute) {
        return StringUtils.isEmpty(gatewayRoute.getUriHealthEndpoint()) ? DEFAULT_HEALTH : gatewayRoute.getUriHealthEndpoint();
    }

    private static HealthStatus getHealthStatus(HttpResponse<HealthCheck> response) {
        if (response.status() != HttpStatus.OK) {
            return DOWN;
        } else {
            return Optional.ofNullable(response.body())
                    .map(HealthCheck::status)
                    .orElse(UNKNOWN);
        }
    }

    @Serdeable
    public record HealthCheck(HealthStatus status) {
    }
}
