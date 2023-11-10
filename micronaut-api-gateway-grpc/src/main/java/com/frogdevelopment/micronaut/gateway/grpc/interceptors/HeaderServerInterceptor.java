package com.frogdevelopment.micronaut.gateway.grpc.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.micronaut.core.order.Ordered;
import jakarta.inject.Singleton;

@Singleton
public class HeaderServerInterceptor implements ServerInterceptor, Ordered {

    static final Context.Key<Metadata> HEADER_CONTEXT = Context.key("headerContext");

    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(final ServerCall<T, R> call, final Metadata headers,
                                                       final ServerCallHandler<T, R> next) {
        final var context = Context.current().withValue(HEADER_CONTEXT, headers);
        final var delegate = Contexts.interceptCall(context, call, headers, next);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
        };
    }

}
