package com.frogdevelopment.micronaut.gateway.http.routed;

import java.util.Optional;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpointCacheLoader;
import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;
import com.frogdevelopment.micronaut.gateway.http.routed.cache.RoutesPathMatcher;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRouteProperties;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
public class RoutedGatewayFactory {

    @Bean
    AsyncLoadingCache<String, Optional<MatchingServiceEndpoint>> serviceEndpointCache(
            final GatewayRouteProperties gatewayRouteProperties) {
        final var pathMatchers = gatewayRouteProperties.getRoutes().entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().getMapping()
                        .stream()
                        .<PathMatcher>map(subRoute -> new RoutesPathMatcher(entry.getValue().getServiceId(), subRoute.getRoute(), subRoute.getContext())))
                .toList();

        return Caffeine.newBuilder()
                .buildAsync(new MatchingServiceEndpointCacheLoader(pathMatchers));
    }

}
