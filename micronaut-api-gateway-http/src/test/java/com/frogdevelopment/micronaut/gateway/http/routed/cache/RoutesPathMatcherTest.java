package com.frogdevelopment.micronaut.gateway.http.routed.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.frogdevelopment.micronaut.gateway.http.core.cache.PathMatcher;

class RoutesPathMatcherTest {

    private static final PathMatcher PATH_MATCHER = new RoutesPathMatcher("service-id", "my-service", null);
    private static final PathMatcher PATH_MATCHER_WITH_CONTEXT = new RoutesPathMatcher("service-id", "my-service", "/my-api");

    @Test
    void should_return_null_when_notMatching() {
        // when
        var serviceEndpoint = PATH_MATCHER.matches("/api/something_else");

        // then
        assertThat(serviceEndpoint).isNull();
    }

    @Test
    void should_return_apiOnly_when_matchingWithoutEndpoint() {
        // when
        var serviceEndpoint = PATH_MATCHER.matches("/api/my-service");

        // then
        assertThat(serviceEndpoint).isNotNull();
        assertThat(serviceEndpoint.serviceId()).isEqualTo("service-id");
        assertThat(serviceEndpoint.endpoint()).isEmpty();
    }

    @Test
    void should_return_apiAndEndpoint_when_matchingWithEndpoint() {
        // when
        var serviceEndpoint = PATH_MATCHER.matches("/api/my-service/my-path/something");

        // then
        assertThat(serviceEndpoint).isNotNull();
        assertThat(serviceEndpoint.serviceId()).isEqualTo("service-id");
        assertThat(serviceEndpoint.endpoint()).isEqualTo("/my-path/something");
    }

    @Test
    void should_return_contextualApiAndEndpoint_when_matchingWithEndpoint() {
        // when
        var serviceEndpoint = PATH_MATCHER_WITH_CONTEXT.matches("/api/my-service/my-path/something");

        // then
        assertThat(serviceEndpoint).isNotNull();
        assertThat(serviceEndpoint.serviceId()).isEqualTo("service-id");
        assertThat(serviceEndpoint.endpoint()).isEqualTo("/my-api/my-path/something");
    }

}
