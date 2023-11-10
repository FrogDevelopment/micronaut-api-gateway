package com.frogdevelopment.micronaut.gateway.grpc.config;

import static io.grpc.ConnectivityState.CONNECTING;
import static io.grpc.ConnectivityState.READY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchIllegalStateException;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;

import lombok.SneakyThrows;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.VoidAnswer2;

import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloServiceGrpc;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.NettyChannelBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.value.PropertyNotFoundException;
import io.micronaut.grpc.channels.GrpcNamedManagedChannelConfiguration;

@ExtendWith(MockitoExtension.class)
class GrpcGatewayFactoryTest {

    @InjectMocks
    private GrpcGatewayFactory grpcGatewayFactory;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private GrpcNamedManagedChannelConfiguration configuration;
    @Mock
    private NettyChannelBuilder nettyChannelBuilder;
    @Mock
    private ManagedChannel managedChannel;

    @Test
    void should_throw_IllegalStateException_when_serviceClassUndefined() {
        // given
        given(configuration.getName()).willReturn("my-service-id");
        given(applicationContext.getRequiredProperty("grpc.channels.my-service-id.class-service", Class.class))
                .willThrow(PropertyNotFoundException.class);

        // when
        var caughtException = catchIllegalStateException(() -> grpcGatewayFactory.serverServiceDefinition(configuration));

        // then
        assertThat(caughtException)
                .hasMessage("Unable to retrieve ServiceDescriptor for my-service-id")
                .hasCauseInstanceOf(PropertyNotFoundException.class);
        then(applicationContext).shouldHaveNoMoreInteractions();
    }

    @Test
    void should_throw_IllegalStateException_when_serviceClassIncorrect() {
        // given
        given(configuration.getName()).willReturn("my-service-id");
        given(applicationContext.getRequiredProperty("grpc.channels.my-service-id.class-service", Class.class))
                .willReturn(ServerServiceDefinition.class);

        // when
        var caughtException = catchIllegalStateException(() -> grpcGatewayFactory.serverServiceDefinition(configuration));

        // then
        assertThat(caughtException)
                .hasMessage("Unable to retrieve ServiceDescriptor for my-service-id")
                .hasCauseInstanceOf(NullPointerException.class);
        then(applicationContext).shouldHaveNoMoreInteractions();
    }

    @Test
    @SneakyThrows
    void should_throw_IllegalStateException_when_unableToConnectToTheChannel() {
        // given
        given(configuration.getName()).willReturn("my-service-id");
        given(applicationContext.getRequiredProperty("grpc.channels.my-service-id.class-service", Class.class)).willReturn(HelloServiceGrpc.class);
        given(applicationContext.createBean(NettyChannelBuilder.class, "my-service-id")).willReturn(nettyChannelBuilder);
        given(configuration.isConnectOnStartup()).willReturn(false);
        given(nettyChannelBuilder.build()).willReturn(managedChannel);
        given(configuration.isConnectOnStartup()).willReturn(Boolean.TRUE);
        given(configuration.getConnectionTimeout()).willReturn(Duration.ofSeconds(5));

        // when
        var caughtException = catchIllegalStateException(() -> grpcGatewayFactory.serverServiceDefinition(configuration));

        // then
        assertThat(caughtException)
                .hasMessage("Unable to connect to the channel: my-service-id")
                .hasNoCause();
    }

    @Test
    @SneakyThrows
    void should_notConnectOnStartup() {
        // given
        given(configuration.getName()).willReturn("my-service-id");
        given(applicationContext.getRequiredProperty("grpc.channels.my-service-id.class-service", Class.class)).willReturn(HelloServiceGrpc.class);
        given(applicationContext.createBean(NettyChannelBuilder.class, "my-service-id")).willReturn(nettyChannelBuilder);
        given(configuration.isConnectOnStartup()).willReturn(false);
        given(nettyChannelBuilder.build()).willReturn(managedChannel);
        given(configuration.isConnectOnStartup()).willReturn(Boolean.FALSE);

        // when
        var serverServiceDefinition = grpcGatewayFactory.serverServiceDefinition(configuration);

        // then
        assertThat(serverServiceDefinition).isNotNull();
        then(configuration).shouldHaveNoMoreInteractions();
        then(managedChannel).shouldHaveNoInteractions();
    }

    @Test
    @SneakyThrows
    void should_connectOnStartup() {
        // given
        given(configuration.getName()).willReturn("my-service-id");
        given(applicationContext.getRequiredProperty("grpc.channels.my-service-id.class-service", Class.class)).willReturn(HelloServiceGrpc.class);
        given(applicationContext.createBean(NettyChannelBuilder.class, "my-service-id")).willReturn(nettyChannelBuilder);
        given(configuration.isConnectOnStartup()).willReturn(false);
        given(nettyChannelBuilder.build()).willReturn(managedChannel);
        given(configuration.isConnectOnStartup()).willReturn(Boolean.TRUE);
        given(configuration.getConnectionTimeout()).willReturn(Duration.ofSeconds(1));

        given(managedChannel.getState(true)).willReturn(CONNECTING);
        given(managedChannel.getState(false))
                .willReturn(CONNECTING)
                .willReturn(READY);
        doAnswer(answerVoid((VoidAnswer2<ConnectivityState, Runnable>) (state, runnable) -> runnable.run()))
                .when(managedChannel)
                .notifyWhenStateChanged(any(), any());

        // when
        var serverServiceDefinition = grpcGatewayFactory.serverServiceDefinition(configuration);

        // then
        assertThat(serverServiceDefinition).isNotNull();
    }

    @Test
    @SneakyThrows
    void should_doNothing_when_closingAndChannelIsAlreadyShutdown() {
        // given
        // noinspection unchecked
        Map<String, ManagedChannel> channels = (Map<String, ManagedChannel>) ReflectionUtils.getField(GrpcGatewayFactory.class, "channels",
                grpcGatewayFactory);
        channels.put("my-service-id", managedChannel);
        given(managedChannel.isShutdown()).willReturn(Boolean.TRUE);

        // when
        grpcGatewayFactory.close();

        // then
        assertThat(channels).isEmpty();
    }

    @Test
    @SneakyThrows
    void should_shutdownChannels_when_closing() {
        // given
        // noinspection unchecked
        Map<String, ManagedChannel> channels = (Map<String, ManagedChannel>) ReflectionUtils.getField(GrpcGatewayFactory.class, "channels",
                grpcGatewayFactory);
        channels.put("my-service-id", managedChannel);
        given(managedChannel.isShutdown()).willReturn(Boolean.FALSE);

        // when
        grpcGatewayFactory.close();

        // then
        assertThat(channels).isEmpty();
        then(managedChannel).should().shutdown();
    }

}
