package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.client.LoadBalancer;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
@RequiredArgsConstructor
final class RouteTargetProvider {

    private final AsyncLoadingCache<String, Optional<MatchingServiceEndpoint>> serviceEndpointCache;
    private final AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;

    @NonNull
    Mono<RouteTarget> findRouteTarget(final String path) {
        return findMatchingServiceEndpoint(path)
                .flatMap(this::toRouteTarget);
    }

    @NonNull
    private Mono<MatchingServiceEndpoint> findMatchingServiceEndpoint(final String path) {
        return Mono.fromFuture(serviceEndpointCache.get(path))
                .flatMap(Mono::justOrEmpty);
    }

    @NonNull
    private Mono<RouteTarget> toRouteTarget(final MatchingServiceEndpoint matchingServiceEndpoint) {
        return Mono.fromFuture(loadBalancerCache.get(matchingServiceEndpoint.serviceId()))
                .map(LoadBalancer::select)
                .flatMap(Mono::from)
                .map(serviceInstance -> toRouteTarget(matchingServiceEndpoint, serviceInstance));
    }

    @NonNull
    private static RouteTarget toRouteTarget(final MatchingServiceEndpoint matchingServiceEndpoint,
                                             final ServiceInstance serviceInstance) {
        return new RouteTarget(
                matchingServiceEndpoint.serviceId(),
                serviceInstance.getURI().getScheme(),
                serviceInstance.getHost(),
                serviceInstance.getPort(),
                matchingServiceEndpoint.endpoint());
    }

}
