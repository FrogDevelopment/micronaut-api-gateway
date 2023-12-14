package com.frogdevelopment.micronaut.gateway.http.core.cache;

import java.net.URI;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.loadbalance.DiscoveryClientLoadBalancerFactory;

@Factory
public class CoreGatewayFactory {

    @Bean
    AsyncLoadingCache<String, LoadBalancer> loadBalancerCache(final DiscoveryClientLoadBalancerFactory factory) {
        return Caffeine.newBuilder().buildAsync(factory::create);
    }

    @Bean
    AsyncLoadingCache<String, URI> uriCache() {
        return Caffeine.newBuilder().buildAsync(URI::create);
    }

}
