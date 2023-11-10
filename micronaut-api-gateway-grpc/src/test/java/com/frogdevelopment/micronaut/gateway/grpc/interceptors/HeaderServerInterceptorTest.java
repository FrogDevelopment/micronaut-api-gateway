package com.frogdevelopment.micronaut.gateway.grpc.interceptors;

import static com.frogdevelopment.micronaut.gateway.grpc.interceptors.HeaderServerInterceptor.HEADER_CONTEXT;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;

@ExtendWith(MockitoExtension.class)
class HeaderServerInterceptorTest {

    private static ExecutorService ANOTHER_THREAD;

    private final HeaderServerInterceptor interceptor = new HeaderServerInterceptor();

    @Mock
    private ServerCall<Object, Object> serverCall;
    @Mock
    private ServerCallHandler<Object, Object> next;

    @BeforeAll
    static void beforeAll() {
        ANOTHER_THREAD = Executors.newSingleThreadExecutor();
    }

    @AfterAll
    static void afterAll() {
        ANOTHER_THREAD.shutdown();
    }

    @Test
    void should_addOriginalHeadersToHeaderContext() {
        // Given
        var baseContext = Context.current();
        var spiedBaseContext = spy(baseContext);
        spiedBaseContext.attach();
        try {
            var headers = new Metadata();
            var customHeader = Metadata.Key.of("x-custom", ASCII_STRING_MARSHALLER);
            var headerValue = UUID.randomUUID().toString();
            headers.put(customHeader, headerValue);

            var rootListener = mock(ServerCall.Listener.class, inv -> {
                // record the current(thread local) grpc context when listener is invoked
                var metadata = HEADER_CONTEXT.get();
                assertThat(metadata).isNotNull();
                assertThat(metadata.get(customHeader)).isEqualTo(headerValue);
                return null;
            });
            //noinspection unchecked
            given(next.startCall(any(), any())).willReturn(rootListener);

            // When
            var listener = interceptor.interceptCall(serverCall, headers, next);
            listener.onReady();

            // Then
        } finally {
            spiedBaseContext.detach(baseContext);
        }
    }

}
