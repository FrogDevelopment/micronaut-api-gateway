package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import io.micronaut.http.MutableHttpRequest;

@FunctionalInterface
public interface RequestCustomizer {

    /**
     * Adding more custom alteration to the request to be routed
     *
     * @param mutableRequest The request
     */
    void customize(final MutableHttpRequest<?> mutableRequest);
}
