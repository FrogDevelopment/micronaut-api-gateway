package com.frogdevelopment.micronaut.gateway.http.core.filter;

import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.frogdevelopment.micronaut.gateway.http.core.proxy.RequestMutator;

import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.simple.SimpleHttpResponseFactory;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ApiFilterTest {

    @InjectMocks
    private ApiFilter apiFilter;

    @Mock
    private RequestMutator requestMutator;
    @Mock
    private ProxyHttpClient proxyHttpClient;
    @Mock
    private HttpRequest<?> httpRequest;
    @Mock
    private MutableHttpRequest<?> mutableHttpRequest;

    @Test
    void should_beOrderedAfterServerFilterPhase_SECURITY() {
        // when
        var order = apiFilter.getOrder();

        // then
        assertThat(order).isGreaterThan(ServerFilterPhase.SECURITY.order());
    }

    @Test
    void should_return_NOT_FOUND_when_noServiceMatchUri() {
        // given
        given(requestMutator.mutate(any())).willReturn(Mono.empty());
        given(httpRequest.getPath()).willReturn("/my/path/endpoint");

        // when
        var optional = Mono.from(apiFilter.filterRequest(httpRequest)).blockOptional();

        // then
        assertThat(optional).isPresent();
        var response = optional.get();
        assertThat(response.getStatus().getCode()).isEqualTo(NOT_FOUND.getCode());
        assertThat(response.getBody(String.class)).hasValue("No service found to proxy [/my/path/endpoint]");
        then(proxyHttpClient).shouldHaveNoInteractions();
    }

    @Test
    void should_return_NOT_FOUND_when_noServiceIsAvailable() {
        // given
        given(requestMutator.mutate(httpRequest)).willReturn(Mono.error(new NoAvailableServiceException("my-service-id")));

        // when
        var optional = Mono.from(apiFilter.filterRequest(httpRequest)).blockOptional();

        // then
        assertThat(optional).isPresent();
        var response = optional.get();
        assertThat(response.getStatus().getCode()).isEqualTo(NOT_FOUND.getCode());
        assertThat(response.getBody(String.class)).hasValue("No available services for ID: my-service-id");
        then(proxyHttpClient).shouldHaveNoInteractions();
    }

    @Test
    void should_return_INTERNAL_SERVER_ERROR_when_exceptionOccurred() {
        // given
        given(requestMutator.mutate(httpRequest)).willReturn(Mono.error(new RuntimeException("for test purpose")));

        // when
        var optional = Mono.from(apiFilter.filterRequest(httpRequest)).blockOptional();

        // then
        assertThat(optional).isPresent();
        var response = optional.get();
        assertThat(response.getStatus().getCode()).isEqualTo(INTERNAL_SERVER_ERROR.getCode());
        assertThat(response.getBody(String.class)).hasValue("An unexpected error occurred: for test purpose");
        then(proxyHttpClient).shouldHaveNoInteractions();
    }

    @Test
    void should_return_proxiedRequest() {
        // given
        given(requestMutator.mutate(httpRequest)).willReturn(Mono.just(mutableHttpRequest));
        var httpResponse = SimpleHttpResponseFactory.INSTANCE.ok("something");
        given(proxyHttpClient.proxy(mutableHttpRequest)).willReturn(Mono.just(httpResponse));

        // when
        var optional = Mono.from(apiFilter.filterRequest(httpRequest)).blockOptional();

        // then
        assertThat(optional).isPresent();
        var response = optional.get();
        assertThat(response.getStatus().getCode()).isEqualTo(OK.getCode());
        assertThat(response.getBody(String.class)).hasValue("something");
    }
}
