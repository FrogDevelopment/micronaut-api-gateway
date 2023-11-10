package com.frogdevelopment.micronaut.gateway.grpc.config;

import static com.frogdevelopment.micronaut.gateway.grpc.GrpcGatewayProxy.proxyMethod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.netty.NettyChannelBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.channels.GrpcNamedManagedChannelConfiguration;
import jakarta.annotation.PreDestroy;

@Slf4j
@Factory
@RequiredArgsConstructor
public class GrpcGatewayFactory implements AutoCloseable {

    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;

    @Override
    @PreDestroy
    public void close() {
        for (final var channel : channels.values()) {
            if (!channel.isShutdown()) {
                channel.shutdown();
            }
        }
        channels.clear();
    }

    /**
     * All these re-routing {@link ServerServiceDefinition} created will be used when creating the {@link io.grpc.ServerBuilder} by
     * {@link io.micronaut.grpc.server.GrpcServerBuilder}.
     */
    @Bean
    @EachBean(GrpcNamedManagedChannelConfiguration.class)
    ServerServiceDefinition serverServiceDefinition(final GrpcNamedManagedChannelConfiguration configuration) {
        final var serviceDescriptor = getServiceDescriptor(configuration.getName());
        final var delegateChannel = createDelegateChannel(configuration);

        log.debug("Building Service Descriptor for [{}]", serviceDescriptor.getName());

        final var builder = ServerServiceDefinition.builder(serviceDescriptor.getName());

        serviceDescriptor.getMethods().forEach(method -> builder.addMethod(proxyMethod(method, delegateChannel)));

        return builder.build();
    }

    private ServiceDescriptor getServiceDescriptor(final String serviceName) {
        try {
            final var grpcServiceClass = applicationContext.getRequiredProperty("grpc.channels." + serviceName + ".class-service", Class.class);
            // noinspection unchecked
            return (ServiceDescriptor) grpcServiceClass.getMethod("getServiceDescriptor").invoke(null);
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to retrieve ServiceDescriptor for " + serviceName, e);
        }
    }

    // adapted from io.micronaut.grpc.channels.GrpcManagedChannelFactory
    private ManagedChannel createDelegateChannel(final GrpcNamedManagedChannelConfiguration channelConfig) {
        final var target = channelConfig.getName();
        return channels.computeIfAbsent(target, channelKey -> {
            final var nettyChannelBuilder = applicationContext.createBean(NettyChannelBuilder.class, target);
            final var channel = nettyChannelBuilder.build();
            if (channelConfig.isConnectOnStartup()) {
                log.debug("Connecting to the channel: {}", target);
                if (!connectOnStartup(channel, channelConfig.getConnectionTimeout())) {
                    throw new IllegalStateException("Unable to connect to the channel: " + target);
                }
                log.debug("Successfully connected to the channel: {}", target);
            }
            return channel;
        });
    }

    private boolean connectOnStartup(final ManagedChannel channel, final Duration timeout) {
        channel.getState(true); // request connection
        final var readyLatch = new CountDownLatch(1);
        waitForReady(channel, readyLatch);
        try {
            return readyLatch.await(timeout.getSeconds(), TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void waitForReady(final ManagedChannel channel, final CountDownLatch readyLatch) {
        final var state = channel.getState(false);
        if (state == ConnectivityState.READY) {
            readyLatch.countDown();
        } else {
            channel.notifyWhenStateChanged(state, () -> waitForReady(channel, readyLatch));
        }
    }

}
