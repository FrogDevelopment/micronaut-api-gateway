package com.frogdevelopment.micronaut.gateway.http.direct.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;

@Slf4j
public class SimplePathMatcher implements PathMatcher {

    private static final String REGEX = "^/api/(?<serviceId>[\\w\\-_]*)(?<endpoint>/?.*)$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public MatchingServiceEndpoint matches(String path) {
        final var matcher = PATTERN.matcher(path);
        final var matches = matcher.matches();

        log.debug("Matching [{}] against pattern '{}': {}", path, PATTERN, matches);
        if (matches) {
            final var serviceId = matcher.group("serviceId");
            final var endpoint = matcher.group("endpoint");

            return new MatchingServiceEndpoint(serviceId, endpoint);
        } else {
            return null;
        }
    }
}
