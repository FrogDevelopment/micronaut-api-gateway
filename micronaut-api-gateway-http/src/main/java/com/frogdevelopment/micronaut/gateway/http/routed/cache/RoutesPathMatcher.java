package com.frogdevelopment.micronaut.gateway.http.routed.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;

@Slf4j
public class RoutesPathMatcher implements PathMatcher {

    private static final String REGEX = "^/api/%s(?<endpoint>/?.*)$";

    private final String uri;
    private final String serviceId;
    private final Pattern pattern;
    private final String context;

    public RoutesPathMatcher(final String uri, final String serviceId, final String route, final String context) {
        this.uri = uri;
        this.serviceId = serviceId;
        this.pattern = Pattern.compile(REGEX.formatted(route));
        this.context = context;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @NonNull
    @Override
    public MatchingServiceEndpoint create(@NonNull final Matcher matcher) {
        final var pathEndpoint = matcher.group("endpoint");
        final var newEndpoint = Optional.ofNullable(context)
                .map(value -> StringUtils.prependUri(value, pathEndpoint))
                .orElse(pathEndpoint);
        return new MatchingServiceEndpoint(uri, serviceId, newEndpoint);
    }

}
