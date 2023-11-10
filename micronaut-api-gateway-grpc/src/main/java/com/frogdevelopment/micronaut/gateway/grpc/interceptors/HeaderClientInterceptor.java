package com.frogdevelopment.micronaut.gateway.grpc.interceptors;

import java.util.Optional;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.micronaut.core.order.Ordered;
import jakarta.inject.Singleton;

@Singleton
public class HeaderClientInterceptor implements ClientInterceptor, Ordered {

    @Override
    public <T, R> ClientCall<T, R> interceptCall(final MethodDescriptor<T, R> method,
                                                 final CallOptions callOptions, final Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(final Listener<R> responseListener, final Metadata headers) {
                Optional.ofNullable(HeaderServerInterceptor.HEADER_CONTEXT.get()).ifPresent(headers::merge);
                super.start(responseListener, headers);
            }
        };
    }
}
