package com.frogdevelopment.micronaut.gateway.http.core;

import com.frogdevelopment.micronaut.gateway.http.core.cache.LoadBalancerCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.loadbalance.DiscoveryClientLoadBalancerFactory;

@Factory
public class GatewayFactory {

    @Bean
    AsyncLoadingCache<String, LoadBalancer> loadBalancerCache(final DiscoveryClientLoadBalancerFactory factory) {
        return Caffeine.newBuilder()
                .buildAsync(new LoadBalancerCacheLoader(factory));
    }

}
