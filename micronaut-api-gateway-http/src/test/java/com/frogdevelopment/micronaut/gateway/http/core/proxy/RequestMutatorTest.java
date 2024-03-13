package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer1;

import io.micronaut.context.BeanProvider;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.uri.UriBuilder;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RequestMutatorTest {

    @InjectMocks
    private RequestMutator requestMutator;

    @Mock
    private RouteTargetProvider routeTargetProvider;
    @Mock
    private BeanProvider<RequestCustomizer> requestCustomizerBeanProvider;

    @Mock
    private NettyHttpRequest<Void> httpRequest;
    @Mock
    private MutableHttpRequest<Void> mutableHttpRequest;

    @Test
    void should_return_emptyMono_when_noServiceDefinitionFound() {
        // given
        given(httpRequest.getPath()).willReturn("/api/unknown-service/my-endpoint");
        given(routeTargetProvider.findRouteTarget("/api/unknown-service/my-endpoint")).willReturn(Mono.empty());

        // when
        var optional = requestMutator.mutate(httpRequest).blockOptional();

        // then
        assertThat(optional).isNotPresent();
    }

    @Test
    void should_return_mutatedRequest() {
        // given
        given(httpRequest.getPath()).willReturn("/api/my-service/my-endpoint");
        given(httpRequest.mutate()).willReturn(mutableHttpRequest);
        //noinspection unchecked
        given(mutableHttpRequest.uri(any(Consumer.class)))
                .will(answer((Answer1<MutableHttpRequest<Void>, Consumer<UriBuilder>>) consumer -> {
                    UriBuilder builder = UriBuilder.of("/api/my-service/my-endpoint");
                    consumer.accept(builder);
                    given(mutableHttpRequest.getUri()).willReturn(builder.build());
                    return mutableHttpRequest;
                }));

        var uri = UriBuilder.of("http://my-host:123").build();
        var routeTarget = new RouteTarget(uri, "my-endpoint");
        given(routeTargetProvider.findRouteTarget("/api/my-service/my-endpoint")).willReturn(Mono.just(routeTarget));

        // when
        var optional = requestMutator.mutate(httpRequest).blockOptional();

        // then
        assertThat(optional).hasValueSatisfying(mutatedRequest -> {
            var mutatedUri = mutatedRequest.getUri();
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(mutatedUri.getScheme()).isEqualTo(routeTarget.uri().getScheme());
                softAssertions.assertThat(mutatedUri.getHost()).isEqualTo(routeTarget.uri().getHost());
                softAssertions.assertThat(mutatedUri.getPort()).isEqualTo(routeTarget.uri().getPort());
                softAssertions.assertThat(mutatedUri.getPath()).isEqualTo("/" + routeTarget.newEndpoint());
            });
        });

        then(requestCustomizerBeanProvider).should().ifResolvable(any());
    }

}
