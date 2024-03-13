package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.frogdevelopment.micronaut.gateway.http.core.cache.MatchingServiceEndpoint;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;

import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.uri.UriBuilder;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RouteTargetProviderTest {

    private RouteTargetProvider routeTargetProvider;

    @Mock
    private AsyncLoadingCache<String, Optional<MatchingServiceEndpoint>> matchingServiceEndpointCache;
    @Mock
    private AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;
    @Mock
    private AsyncLoadingCache<String, URI> uriCache;
    @Mock
    private LoadBalancer loadBalancer;

    @Mock
    private MatchingServiceEndpoint matchingServiceEndpoint;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceInstance serviceInstance;
    @Mock
    private URI matchingURI;

    @BeforeEach()
    void beforeEach() {
        routeTargetProvider = new RouteTargetProvider(matchingServiceEndpointCache, loadBalancerCache, uriCache);
    }

    @Test
    void should_return_emptyMono_when_noMatchingServiceEndpoint_found() {
        // given
        given(matchingServiceEndpointCache.get("unknown-path")).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // when
        var optional = routeTargetProvider.findRouteTarget("unknown-path").blockOptional();

        // then
        assertThat(optional).isNotPresent();
        then(uriCache).shouldHaveNoInteractions();
        then(loadBalancerCache).shouldHaveNoInteractions();
    }

    @Test
    void should_return_emptyMono_when_noServiceInstanceFound() {
        // given
        given(matchingServiceEndpointCache.get("my-path")).willReturn(CompletableFuture.completedFuture(Optional.of(matchingServiceEndpoint)));
        given(matchingServiceEndpoint.uri()).willReturn(null);
        given(matchingServiceEndpoint.serviceId()).willReturn("service-id");
        given(loadBalancerCache.get("service-id")).willReturn(CompletableFuture.completedFuture(null));

        // when
        var optional = routeTargetProvider.findRouteTarget("my-path").blockOptional();

        // then
        assertThat(optional).isNotPresent();
        then(uriCache).shouldHaveNoInteractions();
    }

    @Test
    void should_return_URI_from_configuredURI() {
        // given
        given(matchingServiceEndpointCache.get("my-path")).willReturn(CompletableFuture.completedFuture(Optional.of(matchingServiceEndpoint)));
        given(matchingServiceEndpoint.uri()).willReturn("my-uri");
        given(uriCache.get("my-uri")).willReturn(CompletableFuture.completedFuture(matchingURI));

        // when
        var optional = routeTargetProvider.findRouteTarget("my-path").blockOptional();

        // then
        assertThat(optional).hasValueSatisfying(actual -> {
            assertThat(actual.newEndpoint()).isEqualTo(matchingServiceEndpoint.endpoint());
            assertThat(actual.uri()).isEqualTo(matchingURI);
        });
        then(loadBalancerCache).shouldHaveNoInteractions();
    }

    @Test
    void should_return_URI_from_foundServiceInstance() {
        // given
        given(matchingServiceEndpointCache.get("my-path")).willReturn(CompletableFuture.completedFuture(Optional.of(matchingServiceEndpoint)));
        given(matchingServiceEndpoint.serviceId()).willReturn("service-id");
        given(loadBalancerCache.get("service-id")).willReturn(CompletableFuture.completedFuture(loadBalancer));
        given(loadBalancer.select()).willReturn(Mono.just(serviceInstance));
        given(serviceInstance.getURI()).willReturn(UriBuilder.of("http://my-host:1234").build());

        // when
        var optional = routeTargetProvider.findRouteTarget("my-path").blockOptional();

        // then
        assertThat(optional).hasValueSatisfying(actual -> {
            assertThat(actual.newEndpoint()).isEqualTo(matchingServiceEndpoint.endpoint());
            assertThat(actual.uri().getScheme()).isEqualTo("http");
            assertThat(actual.uri().getHost()).isEqualTo("my-host");
            assertThat(actual.uri().getPort()).isEqualTo(1234);
        });
        then(uriCache).shouldHaveNoInteractions();
    }
}
