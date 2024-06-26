package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import static com.frogdevelopment.micronaut.gateway.http.routed.endpoint.UriHealthCheck.DEFAULT_HEALTH;
import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UNKNOWN;
import static io.micronaut.health.HealthStatus.UP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewaySubRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.endpoint.UriHealthCheck.HealthCheck;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class UriHealthCheckTest {

    private final GatewaySubRoute route = GatewaySubRoute.builder()
            .route("route")
            .context("/foo")
            .build();

    @InjectMocks
    private UriHealthCheck healthCheck;

    @Mock
    private AsyncLoadingCache<String, URI> uriCache;
    @Mock
    private ReactorHttpClient httpClient;

    @Mock
    private GatewayRoute gatewayRoute;
    @Mock
    private URI uri;
    @Mock
    private URI healthUri;
    @Mock
    private HttpResponse<HealthCheck> httpResponse;

    @Test
    void should_return_empty() {
        // given
        given(gatewayRoute.getUri()).willReturn("my_uri");
        given(uriCache.get(gatewayRoute.getUri())).willReturn(CompletableFuture.completedFuture(null));

        // when
        final var checkedHealth = healthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).isEmpty();
    }

    @Test
    void should_return_UP_when_OK_responseWith_UP_body() {
        // given
        given(gatewayRoute.getUri()).willReturn("my_uri");
        given(gatewayRoute.getMapping()).willReturn(List.of(route));
        given(gatewayRoute.getUriHealthEndpoint()).willReturn(null);
        given(uriCache.get(gatewayRoute.getUri())).willReturn(CompletableFuture.completedFuture(uri));
        given(uri.resolve(DEFAULT_HEALTH)).willReturn(healthUri);
        given(healthUri.toString()).willReturn("my_health_uri");
        given(httpClient.exchange("my_health_uri", HealthCheck.class)).willReturn(Mono.just(httpResponse));
        given(httpResponse.status()).willReturn(HttpStatus.OK);
        given(httpResponse.body()).willReturn(new HealthCheck(UP));

        // when
        final var checkedHealth = healthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).hasValueSatisfying(health -> {
            assertThatObject(health.healthStatus()).isEqualTo(UP);
            assertThat(health.uri()).isEqualTo(gatewayRoute.getUri());
            assertThat(health.serviceId()).isNull();
            assertThat(health.mapping()).isEqualTo(gatewayRoute.getMapping());
        });
    }

    @Test
    void should_return_UNKNOWN_when_OK_responseWithoutBody() {
        // given
        given(gatewayRoute.getUri()).willReturn("my_uri");
        given(gatewayRoute.getMapping()).willReturn(List.of(route));
        given(gatewayRoute.getUriHealthEndpoint()).willReturn(null);
        given(uriCache.get(gatewayRoute.getUri())).willReturn(CompletableFuture.completedFuture(uri));
        given(uri.resolve(DEFAULT_HEALTH)).willReturn(healthUri);
        given(healthUri.toString()).willReturn("my_health_uri");
        given(httpClient.exchange("my_health_uri", HealthCheck.class)).willReturn(Mono.just(httpResponse));
        given(httpResponse.status()).willReturn(HttpStatus.OK);
        given(httpResponse.body()).willReturn(null);

        // when
        final var checkedHealth = healthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).hasValueSatisfying(health -> {
            assertThatObject(health.healthStatus()).isEqualTo(UNKNOWN);
            assertThat(health.uri()).isEqualTo(gatewayRoute.getUri());
            assertThat(health.serviceId()).isNull();
            assertThat(health.mapping()).isEqualTo(gatewayRoute.getMapping());
        });
    }

    @Test
    void should_return_DOWN_when_not_OK() {
        // given
        given(gatewayRoute.getUri()).willReturn("my_uri");
        given(gatewayRoute.getMapping()).willReturn(List.of(route));
        given(gatewayRoute.getUriHealthEndpoint()).willReturn(null);
        given(uriCache.get(gatewayRoute.getUri())).willReturn(CompletableFuture.completedFuture(uri));
        given(uri.resolve(DEFAULT_HEALTH)).willReturn(healthUri);
        given(healthUri.toString()).willReturn("my_health_uri");
        given(httpClient.exchange("my_health_uri", HealthCheck.class)).willReturn(Mono.just(httpResponse));
        given(httpResponse.status()).willReturn(HttpStatus.SERVICE_UNAVAILABLE);

        // when
        final var checkedHealth = healthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).hasValueSatisfying(health -> {
            assertThatObject(health.healthStatus()).isEqualTo(DOWN);
            assertThat(health.uri()).isEqualTo(gatewayRoute.getUri());
            assertThat(health.serviceId()).isNull();
            assertThat(health.mapping()).isEqualTo(gatewayRoute.getMapping());
        });
    }

    @Test
    void should_return_DOWN_when_errorOccurs() {
        // given
        given(gatewayRoute.getUri()).willReturn("service_id");
        given(gatewayRoute.getMapping()).willReturn(List.of(route));
        given(gatewayRoute.getUriHealthEndpoint()).willReturn("my_health");
        given(uriCache.get(gatewayRoute.getUri())).willReturn(CompletableFuture.completedFuture(uri));
        given(uri.resolve("my_health")).willReturn(healthUri);
        given(healthUri.toString()).willReturn("my_health_uri");
        given(httpClient.exchange("my_health_uri", HealthCheck.class)).willThrow(RuntimeException.class);

        // when
        final var checkedHealth = healthCheck.checkHealth(gatewayRoute).blockOptional();

        // then
        assertThat(checkedHealth).hasValueSatisfying(health -> {
            assertThatObject(health.healthStatus()).isEqualTo(DOWN);
            assertThat(health.uri()).isEqualTo(gatewayRoute.getUri());
            assertThat(health.serviceId()).isNull();
            assertThat(health.mapping()).isEqualTo(gatewayRoute.getMapping());
        });
        then(httpClient).shouldHaveNoMoreInteractions();
    }

}
