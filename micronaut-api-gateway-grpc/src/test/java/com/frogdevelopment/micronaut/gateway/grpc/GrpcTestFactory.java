package com.frogdevelopment.micronaut.gateway.grpc;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;

import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloServiceGrpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.server.GrpcEmbeddedServer;

@Slf4j
@Factory
class GrpcTestFactory implements AutoCloseable{

    private static final String EXTERNAL_CHANNEL = "external-channel";
    private final List<ManagedChannel> channels = new LinkedList<>();

    // using a custom ManagedChannel to simulate the call from an external application => not having api-gateway interceptors
    @Named(EXTERNAL_CHANNEL)
    @Bean
    ManagedChannel externalChannel(final GrpcEmbeddedServer grpcEmbeddedServer) {
        final var managedChannel = NettyChannelBuilder.forAddress(grpcEmbeddedServer.getHost(), grpcEmbeddedServer.getPort())
                .usePlaintext()
                .build();
        channels.add(managedChannel);
        return managedChannel;
    }

    @Bean
    HelloServiceGrpc.HelloServiceBlockingStub blockingStub(@Named(EXTERNAL_CHANNEL) Channel externalChannel) {
        return HelloServiceGrpc.newBlockingStub(externalChannel);
    }

    @Bean
    HelloServiceGrpc.HelloServiceStub nonBlockingStub(@Named(EXTERNAL_CHANNEL) Channel externalChannel) {
        return HelloServiceGrpc.newStub(externalChannel);
    }

    @Override
    @PreDestroy
    public void close() {
        for (ManagedChannel channel : channels) {
            if (!channel.isShutdown()) {
                try {
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    if (log.isWarnEnabled()) {
                        log.warn("Error shutting down GRPC channel: {}", e.getMessage(), e);
                    }
                }
            }
        }
        channels.clear();
    }
}
