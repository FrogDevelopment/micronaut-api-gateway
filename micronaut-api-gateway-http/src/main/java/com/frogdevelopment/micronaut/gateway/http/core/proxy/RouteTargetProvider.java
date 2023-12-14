package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import lombok.RequiredArgsConstructor;

import java.net.URI;
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

    private final AsyncLoadingCache<String, Optional<MatchingServiceEndpoint>> matchingServiceEndpointCache;
    private final AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;
    private final AsyncLoadingCache<String, URI> uriCache;

    @NonNull
    Mono<RouteTarget> findRouteTarget(final String path) {
        return findMatchingServiceEndpoint(path)
                .flatMap(matchingServiceEndpoint -> toUri(matchingServiceEndpoint)
                        .map(uri -> new RouteTarget(uri, matchingServiceEndpoint.endpoint())));
    }

    @NonNull
    private Mono<MatchingServiceEndpoint> findMatchingServiceEndpoint(final String path) {
        return Mono.fromFuture(matchingServiceEndpointCache.get(path))
                .flatMap(Mono::justOrEmpty);
    }

    @NonNull
    private Mono<URI> toUri(final MatchingServiceEndpoint matchingServiceEndpoint) {
        if (matchingServiceEndpoint.uri() != null) {
            return Mono.fromFuture(uriCache.get(matchingServiceEndpoint.uri()));
        } else {
            return getServiceInstance(matchingServiceEndpoint)
                    .map(ServiceInstance::getURI);
        }
    }

    private Mono<ServiceInstance> getServiceInstance(MatchingServiceEndpoint matchingServiceEndpoint) {
        return Mono.fromFuture(loadBalancerCache.get(matchingServiceEndpoint.serviceId()))
                .map(LoadBalancer::select)
                .flatMap(Mono::from);
    }

}
