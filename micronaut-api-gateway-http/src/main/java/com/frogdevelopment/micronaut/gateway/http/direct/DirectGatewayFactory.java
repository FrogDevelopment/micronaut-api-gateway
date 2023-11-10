package com.frogdevelopment.micronaut.gateway.http.direct;

import java.util.List;
import java.util.Optional;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpointCacheLoader;
import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;
import com.frogdevelopment.micronaut.gateway.http.direct.cache.SimplePathMatcher;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
public class DirectGatewayFactory {

    @Bean
    AsyncLoadingCache<String, Optional<MatchingServiceEndpoint>> serviceEndpointCache() {
        final var pathMatchers = List.<PathMatcher>of(new SimplePathMatcher());

        return Caffeine.newBuilder()
                .buildAsync(new MatchingServiceEndpointCacheLoader(pathMatchers));
    }
}
