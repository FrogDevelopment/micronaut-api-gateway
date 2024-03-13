package com.frogdevelopment.micronaut.gateway.http.core.cache;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

public interface PathMatcher {

    @NonNull
    Pattern getPattern();

    @NonNull
    Logger getLogger();

    @Nullable
    default MatchingServiceEndpoint matches(@NonNull String path) {
        final var matcher = getPattern().matcher(path);
        final var matches = matcher.matches();

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Matching [{}] against pattern '{}': {}", path, getPattern(), matches);
        }

        if (matches) {
            return create(matcher);
        } else {
            return null;
        }
    }

    @NonNull
    MatchingServiceEndpoint create(@NonNull final Matcher matcher);
}
