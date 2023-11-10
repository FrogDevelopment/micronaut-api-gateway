package com.frogdevelopment.micronaut.gateway.http.core.cache;

import lombok.RequiredArgsConstructor;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.benmanes.caffeine.cache.CacheLoader;

import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.loadbalance.DiscoveryClientLoadBalancerFactory;

@RequiredArgsConstructor
public class LoadBalancerCacheLoader implements CacheLoader<String, LoadBalancer> {

    private final DiscoveryClientLoadBalancerFactory loadBalancerFactory;

    @Override
    public @Nullable LoadBalancer load(String serviceId) {
        return loadBalancerFactory.create(serviceId);
    }

}
