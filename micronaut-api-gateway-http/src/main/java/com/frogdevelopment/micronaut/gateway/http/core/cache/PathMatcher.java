package com.frogdevelopment.micronaut.gateway.http.core.cache;

import io.micronaut.core.annotation.Nullable;

public interface PathMatcher {

    @Nullable
    MatchingServiceEndpoint matches(String path);
}
