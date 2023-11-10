package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import io.micronaut.http.MutableHttpRequest;

@FunctionalInterface
public interface RequestCustomizer {

    void customize(final MutableHttpRequest<?> mutableRequest);
}
