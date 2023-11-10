package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RouteTargetProviderTest {

    private RouteTargetProvider routeTargetProvider;

    @Mock
    private AsyncLoadingCache<String, Optional<MatchingServiceEndpoint>> matchingServiceEndpointCache;
    @Mock
    private AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;
    @Mock
    private LoadBalancer loadBalancer;

    @Mock
    private MatchingServiceEndpoint matchingServiceEndpoint;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceInstance serviceInstance;

    @BeforeEach()
    void beforeEach() {
        routeTargetProvider = new RouteTargetProvider(matchingServiceEndpointCache, loadBalancerCache);
    }

    @Test
    void should_return_emptyMono_when_noServiceEndpointFound() {
        // given
        given(matchingServiceEndpointCache.get("unknown-path")).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // when
        var optional = routeTargetProvider.findRouteTarget("unknown-path").blockOptional();

        // then
        assertThat(optional).isNotPresent();
    }

    @Test
    void should_return_emptyMono_when_noServiceInstanceFound() {
        // given
        given(matchingServiceEndpointCache.get("my-path")).willReturn(CompletableFuture.completedFuture(Optional.of(matchingServiceEndpoint)));
        given(matchingServiceEndpoint.serviceId()).willReturn("service-id");
        given(loadBalancerCache.get("service-id")).willReturn(CompletableFuture.completedFuture(null));

        // when
        var optional = routeTargetProvider.findRouteTarget("my-path").blockOptional();

        // then
        assertThat(optional).isNotPresent();
    }

    @Test
    void should_return_Mono_when_serviceDefinitionFound() {
        // given
        given(matchingServiceEndpointCache.get("my-path")).willReturn(CompletableFuture.completedFuture(Optional.of(matchingServiceEndpoint)));
        given(matchingServiceEndpoint.serviceId()).willReturn("service-id");
        given(loadBalancerCache.get("service-id")).willReturn(CompletableFuture.completedFuture(loadBalancer));
        given(loadBalancer.select()).willReturn(Mono.just(serviceInstance));
        given(serviceInstance.getURI().getScheme()).willReturn("http");
        given(serviceInstance.getHost()).willReturn("my-host");
        given(serviceInstance.getPort()).willReturn(1234);

        // when
        var optional = routeTargetProvider.findRouteTarget("my-path").blockOptional();

        // then
        assertThat(optional).hasValueSatisfying(actual -> {
            assertThat(actual.newEndpoint()).isEqualTo(matchingServiceEndpoint.endpoint());
            assertThat(actual.serviceId()).isEqualTo("service-id");
            assertThat(actual.scheme()).isEqualTo("http");
            assertThat(actual.host()).isEqualTo("my-host");
            assertThat(actual.port()).isEqualTo(1234);
        });
    }
}
