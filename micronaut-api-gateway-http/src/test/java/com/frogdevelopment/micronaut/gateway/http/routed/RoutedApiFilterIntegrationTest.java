package com.frogdevelopment.micronaut.gateway.http.routed;

import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CompletableFuture;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;

import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.simple.SimpleHttpResponseFactory;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@Tag("integrationTest")
@ExtendWith(MockitoExtension.class)
@MicronautTest(environments = "routed")
class RoutedApiFilterIntegrationTest {

    @MockBean(ProxyHttpClient.class)
    ProxyHttpClient proxyHttpClient() {
        return mock(ProxyHttpClient.class);
    }

    @MockBean(AsyncLoadingCache.class)
    AsyncLoadingCache<String, LoadBalancer> loadBalancerCache() {
        return Mockito.mock(AsyncLoadingCache.class);
    }

    @Inject
    private RequestSpecification requestSpecification;

    @Inject
    private ProxyHttpClient proxyHttpClient;
    @Inject
    private AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;
    @Mock
    private LoadBalancer loadBalancer;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceInstance serviceInstance;
    @Captor
    private ArgumentCaptor<MutableHttpRequest<?>> requestArgumentCaptor;

    @Test
    void should_return_NOT_FOUND_when_filterDoesNotMatch() {
        // when
        requestSpecification.when()
                .get("/wrong/my-service/my-endpoint");

        // then
        requestSpecification.then()
                .statusCode(NOT_FOUND.getCode())
                .body("message", equalTo(NOT_FOUND.getReason()));
        then(proxyHttpClient).shouldHaveNoInteractions();
    }

    @Test
    void should_proxy_when_filterMatches() {
        // given
        given(loadBalancerCache.get("my-service-id")).willReturn(CompletableFuture.completedFuture(loadBalancer));
        given(loadBalancer.select()).willReturn(Mono.just(serviceInstance));
        given(serviceInstance.getURI().getScheme()).willReturn("https");
        given(serviceInstance.getHost()).willReturn("service-host");
        given(serviceInstance.getPort()).willReturn(1234);
        var body = """
                { "field_1":"value_1", "field_2": { "field_2_1:true } }""";
        var httpResponse = SimpleHttpResponseFactory.INSTANCE.ok(body);

        given(proxyHttpClient.proxy(requestArgumentCaptor.capture())).willReturn(Mono.just(httpResponse));

        requestSpecification
                .given()
                .param("test_1", "value_1")
                .and()
                .param("test_2", "value_2")
                .and()
                .header("X-client-id", "custom-header");

        // when
        requestSpecification
                .when()
                .get("/api/foo/bar/my-endpoint");

        // then
        requestSpecification
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body(IsEqual.equalTo(body));

        var httpRequest = requestArgumentCaptor.getValue();
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(httpRequest.getUri().getScheme()).isEqualTo("https");
            softAssertions.assertThat(httpRequest.getUri().getHost()).isEqualTo("service-host");
            softAssertions.assertThat(httpRequest.getUri().getPort()).isEqualTo(1234);
            softAssertions.assertThat(httpRequest.getPath()).isEqualTo("/foo-bar/my-endpoint");
            softAssertions.assertThat(httpRequest.getParameters().values()).hasSize(2);
            softAssertions.assertThat(httpRequest.getParameters().get("test_1")).isEqualTo("value_1");
            softAssertions.assertThat(httpRequest.getParameters().get("test_2")).isEqualTo("value_2");
            softAssertions.assertThat(httpRequest.getHeaders().get("X-client-id")).isEqualTo("custom-header");
        });
    }

}
