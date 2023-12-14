package com.frogdevelopment.micronaut.gateway.http.direct;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.OK;
import static org.mockito.BDDMockito.given;

import java.util.concurrent.CompletableFuture;
import jakarta.inject.Inject;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import reactor.core.publisher.Mono;

@WireMockTest(httpPort = 51180)
@Tag("integrationTest")
@ExtendWith(MockitoExtension.class)
@MicronautTest(environments = "direct")
class DirectApiFilterIntegrationTest {

    @MockBean(AsyncLoadingCache.class)
    AsyncLoadingCache<String, LoadBalancer> loadBalancerCache() {
        return Mockito.mock(AsyncLoadingCache.class);
    }

    @Inject
    private RequestSpecification requestSpecification;

    @Inject
    private AsyncLoadingCache<String, LoadBalancer> loadBalancerCache;
    @Mock
    private LoadBalancer loadBalancer;
    @Mock
    private ServiceInstance serviceInstance;

    @Test
    void should_return_NOT_FOUND_when_filterDoesNotMatch() {
        // when
        var response = requestSpecification
                .when()
                .get("/wrong/my-service/my-endpoint");

        // then
        response.then()
                .statusCode(NOT_FOUND.getCode());
    }

    @Test
    void should_proxy_when_filterMatches(WireMockRuntimeInfo wmRuntimeInfo) {
        // given
        given(loadBalancerCache.get("my-service-id")).willReturn(CompletableFuture.completedFuture(loadBalancer));
        given(loadBalancer.select()).willReturn(Mono.just(serviceInstance));
        var uri = UriBuilder.of(wmRuntimeInfo.getHttpBaseUrl()).build();
        given(serviceInstance.getURI()).willReturn(uri);

        var body = """
                { "field_1":"value_1", "field_2": { "field_2_1:true } }""";
        var wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get(urlPathEqualTo("/my-endpoint"))
                .withQueryParam("test_1", WireMock.equalTo("value_1"))
                .withQueryParam("test_2", WireMock.equalTo("value_2"))
                .withHeader("X-client-id", WireMock.equalTo("custom-header"))
                .willReturn(ok(body)));

        // when
        var response = requestSpecification
                .when()
                .given()
                .param("test_1", "value_1")
                .and()
                .param("test_2", "value_2")
                .and()
                .header("X-client-id", "custom-header")
                .get("/api/my-service-id/my-endpoint");

        // then
        response
                .then()
                .statusCode(OK.getCode())
                .body(IsEqual.equalTo(body));
    }
}
