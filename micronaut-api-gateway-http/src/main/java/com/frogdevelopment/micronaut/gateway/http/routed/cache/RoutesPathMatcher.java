package com.frogdevelopment.micronaut.gateway.http.routed.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Pattern;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;

@Slf4j
public class RoutesPathMatcher implements PathMatcher {

    private static final String REGEX = "^/api/%s(?<endpoint>/?.*)$";

    private final String serviceId;
    private final Pattern pattern;
    private final String context;

    public RoutesPathMatcher(final String serviceId, final String route, final String context) {
        this.serviceId = serviceId;
        this.pattern = Pattern.compile(REGEX.formatted(route));
        this.context = context;
    }

    @Override
    public MatchingServiceEndpoint matches(final String path) {
        final var matcher = pattern.matcher(path);
        final var matches = matcher.matches();

        log.debug("Matching [{}] against pattern '{}': {}", path, pattern.pattern(), matches);
        if (matches) {
            final var pathEndpoint = matcher.group("endpoint");
            final var newEndpoint = Optional.ofNullable(context)
                    .map(value -> value + pathEndpoint)
                    .orElse(pathEndpoint);
            return new MatchingServiceEndpoint(serviceId, newEndpoint);
        }

        return null;
    }

}
