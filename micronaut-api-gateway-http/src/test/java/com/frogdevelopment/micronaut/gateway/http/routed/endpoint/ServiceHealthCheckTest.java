package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewaySubRoute;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;

import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.client.LoadBalancer;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ServiceHealthCheckTest {

    private final GatewaySubRoute route = GatewaySubRoute.builder()
            .route("route")
            .context("/foo")
            .build();

    @InjectMocks
    private ServiceHealthCheck serviceHealthCheck;

    @Mock
    private AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;

    @Mock
    private LoadBalancer loadBalancer;
    @Mock
    private GatewayRoute gatewayRoute;
    @Mock
    private ServiceInstance serviceInstance;

    @Test
    void should_return_empty() {
        // given
        given(gatewayRoute.getServiceId()).willReturn("service_id");
        given(loadBalancerCache.get(gatewayRoute.getServiceId())).willReturn(CompletableFuture.completedFuture(null));

        // when
        final var checkedHealth = serviceHealthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).isEmpty();
    }

    @Test
    void should_return_UP_when_serviceHealthy() {
        // given
        given(gatewayRoute.getServiceId()).willReturn("service_id");
        given(gatewayRoute.getMapping()).willReturn(List.of(route));
        given(loadBalancerCache.get("service_id")).willReturn(CompletableFuture.completedFuture(loadBalancer));
        given(loadBalancer.select()).willReturn(Mono.just(serviceInstance));
        given(serviceInstance.getHealthStatus()).willReturn(UP);

        // when
        final var checkedHealth = serviceHealthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).hasValueSatisfying(health -> {
            assertThatObject(health.healthStatus()).isEqualTo(UP);
            assertThat(health.uri()).isNull();
            assertThat(health.serviceId()).isEqualTo(gatewayRoute.getServiceId());
            assertThat(health.mapping()).isEqualTo(gatewayRoute.getMapping());
        });
    }

    @Test
    void should_return_DOWN_when_serviceUnhealthy() {
        // given
        given(gatewayRoute.getServiceId()).willReturn("service_id");
        given(gatewayRoute.getMapping()).willReturn(List.of(route));
        given(loadBalancerCache.get("service_id")).willReturn(CompletableFuture.completedFuture(loadBalancer));
        given(loadBalancer.select()).willReturn(Mono.just(serviceInstance));
        given(serviceInstance.getHealthStatus()).willReturn(DOWN);

        // when
        final var checkedHealth = serviceHealthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).hasValueSatisfying(health -> {
            assertThatObject(health.healthStatus()).isEqualTo(DOWN);
            assertThat(health.uri()).isNull();
            assertThat(health.serviceId()).isEqualTo(gatewayRoute.getServiceId());
            assertThat(health.mapping()).isEqualTo(gatewayRoute.getMapping());
        });
    }

    @Test
    void should_return_DOWN_when_errorOccurs() {
        // given
        given(gatewayRoute.getServiceId()).willReturn("service_id");
        given(gatewayRoute.getMapping()).willReturn(List.of(route));
        given(loadBalancerCache.get("service_id")).willReturn(CompletableFuture.completedFuture(loadBalancer));
        given(loadBalancer.select()).willThrow(RuntimeException.class);

        // when
        final var checkedHealth = serviceHealthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).hasValueSatisfying(health -> {
            assertThatObject(health.healthStatus()).isEqualTo(DOWN);
            assertThat(health.uri()).isNull();
            assertThat(health.serviceId()).isEqualTo(gatewayRoute.getServiceId());
            assertThat(health.mapping()).isEqualTo(gatewayRoute.getMapping());
        });
        then(serviceInstance).shouldHaveNoMoreInteractions();
    }

}
