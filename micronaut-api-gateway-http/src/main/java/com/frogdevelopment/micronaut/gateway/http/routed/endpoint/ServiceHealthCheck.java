package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import static io.micronaut.health.HealthStatus.DOWN;

import lombok.RequiredArgsConstructor;

import jakarta.inject.Singleton;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;

import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.client.LoadBalancer;
import reactor.core.publisher.Mono;

@Singleton
@RequiredArgsConstructor
class ServiceHealthCheck {

    private final AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;

    Mono<ServiceHealth> checkHealth(final GatewayRoute gatewayRoute) {
        return Mono.fromFuture(loadBalancerCache.get(gatewayRoute.getServiceId()))
                .flatMap(loadBalancer -> Mono.from(loadBalancer.select()))
                .map(ServiceInstance::getHealthStatus)
                .onErrorReturn(DOWN)
                .map(healthStatus -> ServiceHealth.fromService(gatewayRoute, healthStatus));
    }
}
