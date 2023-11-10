package com.frogdevelopment.micronaut.gateway.grpc.interceptors;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

@ExtendWith(MockitoExtension.class)
class HeaderClientInterceptorTest {

    private static ExecutorService ANOTHER_THREAD;

    private final HeaderClientInterceptor interceptor = new HeaderClientInterceptor();

    @Mock
    private MethodDescriptor<Object, Object> methodDescriptor;
    @Mock
    private CallOptions callOptions;
    @Mock
    private Channel nextChannel;
    @Mock
    private ClientCall<Object, Object> clientCall;
    @Mock
    private ClientCall.Listener<Object> responseListener;

    @BeforeAll
    static void beforeAll() {
        ANOTHER_THREAD = Executors.newSingleThreadExecutor();
    }

    @AfterAll
    static void afterAll() {
        ANOTHER_THREAD.shutdown();
    }

    @Test
    void should_addOriginalHeadersToProxiedRequest() {
        // given
        var baseContext = Context.current();
        var extraHeader = new Metadata();
        var customHeader = Metadata.Key.of("x-custom", ASCII_STRING_MARSHALLER);
        var headerValue = UUID.randomUUID().toString();
        extraHeader.put(customHeader, headerValue);
        var contextWithData = baseContext.withValue(HeaderServerInterceptor.HEADER_CONTEXT, extraHeader);
        try {
            contextWithData.attach();
            given(nextChannel.newCall(methodDescriptor, callOptions)).willReturn(clientCall);
            var headers = new Metadata();

            // when
            var newCall = interceptor.interceptCall(methodDescriptor, callOptions, nextChannel);

            // then
            assertThat(newCall).isInstanceOf(ForwardingClientCall.SimpleForwardingClientCall.class);

            // when
            newCall.start(responseListener, headers);

            // then
            assertThat(headers.keys()).hasSize(1);
            assertThat(headers.get(customHeader)).isEqualTo(headerValue);
        } finally {
            contextWithData.detach(baseContext);
        }
    }
}
