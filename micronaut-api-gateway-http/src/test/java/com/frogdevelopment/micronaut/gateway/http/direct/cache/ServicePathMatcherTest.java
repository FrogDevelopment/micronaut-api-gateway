package com.frogdevelopment.micronaut.gateway.http.direct.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;

class ServicePathMatcherTest {

    private static final PathMatcher PATH_MATCHER = new ServicePathMatcher();


    @Test
    void should_return_null_when_notMatching() {
        // when
        var serviceEndpoint = PATH_MATCHER.matches("/apx/my-service-id");

        // then
        assertThat(serviceEndpoint).isNull();
    }

    @Test
    void should_return_apiOnly_when_matchingWithoutEndpoint() {
        // when
        var serviceEndpoint = PATH_MATCHER.matches("/api/my-service-id");

        // then
        assertThat(serviceEndpoint).isNotNull();
        assertThat(serviceEndpoint.serviceId()).isEqualTo("my-service-id");
        assertThat(serviceEndpoint.endpoint()).isEmpty();
    }

    @Test
    void should_return_apiAndEndpoint_when_matchingWithEndpoint() {
        // when
        var serviceEndpoint = PATH_MATCHER.matches("/api/my-service-id/my-path/something");

        // then
        assertThat(serviceEndpoint).isNotNull();
        assertThat(serviceEndpoint.serviceId()).isEqualTo("my-service-id");
        assertThat(serviceEndpoint.endpoint()).isEqualTo("/my-path/something");
    }
}
