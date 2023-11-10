package com.frogdevelopment.micronaut.gateway.http.core.cache;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.CacheLoader;

import io.micronaut.core.annotation.NonNull;

/**
 * Cache loader to avoid matching patch against defined routes each time.<br/>
 * Note: using {@link Optional} for negative caching, as {@code empty} is a wanted result (no match) while {@code null} is considered as not yet
 * tested/loaded
 */
@RequiredArgsConstructor
public class ServiceEndpointCacheLoader implements CacheLoader<String, Optional<MatchingServiceEndpoint>> {

    private final List<PathMatcher> pathMatchers;

    @Override
    public Optional<MatchingServiceEndpoint> load(@NonNull final String path) {
        return pathMatchers
                .stream()
                .map(pathMatcher -> pathMatcher.matches(path))
                .filter(Objects::nonNull)
                .findFirst();
    }

}
