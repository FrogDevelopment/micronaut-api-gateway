package com.frogdevelopment.micronaut.gateway.grpc;

import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.frogdevelopment.micronaut.gateway.grpc.marshaller.ByteArrayMarshaller;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Context;
import io.grpc.MethodDescriptor;
import io.grpc.ServerMethodDefinition;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCalls;
import io.micronaut.core.annotation.NonNull;

/**
 * Creates {@link ServerMethodDefinition} that proxy calls to a delegate {@link Channel}.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GrpcGatewayProxy {

    @NonNull
    public static ServerMethodDefinition<byte[], byte[]> proxyMethod(
            final MethodDescriptor<?, ?> originalMethodDescriptor,
            final Channel delegateChannel) {

        final var byteArrayMarshaller = new ByteArrayMarshaller();
        final var methodDescriptor = originalMethodDescriptor.toBuilder(byteArrayMarshaller, byteArrayMarshaller).build();

        final var serverCallHandler = switch (methodDescriptor.getType()) {
            case UNARY -> asyncUnaryCall(delegateUnaryCall(methodDescriptor, delegateChannel));
            case CLIENT_STREAMING ->
                    asyncClientStreamingCall(delegateClientStreamingCall(methodDescriptor, delegateChannel));
            case SERVER_STREAMING ->
                    asyncServerStreamingCall(delegateServerStreamingCall(methodDescriptor, delegateChannel));
            case BIDI_STREAMING -> asyncBidiStreamingCall(delegateBidiStreamingCall(methodDescriptor, delegateChannel));
            case UNKNOWN ->
                    throw new GrpcGatewayException("%s has unknown type".formatted(methodDescriptor.getFullMethodName()));
        };

        return ServerMethodDefinition.create(methodDescriptor, serverCallHandler);
    }

    @NonNull
    private static <T> ServerCalls.UnaryMethod<T, T> delegateUnaryCall(
            final MethodDescriptor<T, T> methodDescriptor,
            final Channel delegateChannel) {
        return (request, responseObserver) -> {
            log.debug("Redirecting UNARY call for [{}] to [{}]", methodDescriptor.getFullMethodName(), delegateChannel.authority());
            final var context = Context.current();
            final var callOptions = CallOptions.DEFAULT
                    .withDeadline(context.getDeadline());
            final var clientCall = delegateChannel.newCall(methodDescriptor, callOptions);
            ClientCalls.asyncUnaryCall(clientCall, request, responseObserver);
        };
    }

    @NonNull
    private static <T> ServerCalls.ClientStreamingMethod<T, T> delegateClientStreamingCall(
            final MethodDescriptor<T, T> methodDescriptor,
            final Channel delegateChannel) {
        return responseObserver -> {
            log.debug("Redirecting CLIENT_STREAMING call for [{}] to [{}]", methodDescriptor.getFullMethodName(), delegateChannel.authority());
            final var context = Context.current();
            final var callOptions = CallOptions.DEFAULT
                    .withDeadline(context.getDeadline());
            final var clientCall = delegateChannel.newCall(methodDescriptor, callOptions);
            return ClientCalls.asyncClientStreamingCall(clientCall, responseObserver);
        };
    }

    @NonNull
    private static <T> ServerCalls.ServerStreamingMethod<T, T> delegateServerStreamingCall(
            final MethodDescriptor<T, T> methodDescriptor,
            final Channel delegateChannel) {
        return (request, responseObserver) -> {
            log.debug("Redirecting SERVER_STREAMING call for [{}] to [{}]", methodDescriptor.getFullMethodName(), delegateChannel.authority());
            final var context = Context.current();
            final var callOptions = CallOptions.DEFAULT
                    .withDeadline(context.getDeadline());
            final var clientCall = delegateChannel.newCall(methodDescriptor, callOptions);
            ClientCalls.asyncServerStreamingCall(clientCall, request, responseObserver);
        };
    }

    @NonNull
    private static <T> ServerCalls.BidiStreamingMethod<T, T> delegateBidiStreamingCall(
            final MethodDescriptor<T, T> methodDescriptor,
            final Channel delegateChannel) {
        return responseObserver -> {
            log.debug("Redirecting BIDI_STREAMING call for [{}] to [{}]", methodDescriptor.getFullMethodName(), delegateChannel.authority());
            final var context = Context.current();
            final var callOptions = CallOptions.DEFAULT
                    .withDeadline(context.getDeadline());
            final var clientCall = delegateChannel.newCall(methodDescriptor, callOptions);
            return ClientCalls.asyncBidiStreamingCall(clientCall, responseObserver);
        };
    }
}
