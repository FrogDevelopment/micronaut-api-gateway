package com.frogdevelopment.micronaut.gateway.http.direct.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;

import io.micronaut.core.annotation.NonNull;

@Slf4j
public class ServicePathMatcher implements PathMatcher {

    private static final String REGEX = "^/api/(?<serviceId>[\\w\\-_]*)(?<endpoint>/?.*)$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @NonNull
    @Override
    public MatchingServiceEndpoint create(@NonNull final Matcher matcher) {
        final var serviceId = matcher.group("serviceId");
        final var endpoint = matcher.group("endpoint");
        return new MatchingServiceEndpoint(null, serviceId, endpoint);
    }
}
