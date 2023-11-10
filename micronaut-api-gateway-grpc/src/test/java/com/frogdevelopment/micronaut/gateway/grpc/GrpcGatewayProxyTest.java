package com.frogdevelopment.micronaut.gateway.grpc;

import static com.frogdevelopment.micronaut.gateway.grpc.GrpcGatewayProxy.proxyMethod;
import static io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING;
import static io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING;
import static io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING;
import static io.grpc.MethodDescriptor.MethodType.UNARY;
import static io.grpc.MethodDescriptor.MethodType.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer2;

import com.frogdevelopment.micronaut.gateway.grpc.marshaller.ByteArrayMarshaller;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

@ExtendWith(MockitoExtension.class)
class GrpcGatewayProxyTest {

    @Mock
    private MethodDescriptor<byte[], byte[]> originalMethodDescriptor;

    @Mock
    private Channel delegateChannel;

    private void givenMethod(final MethodDescriptor.MethodType methodType) {
        given(originalMethodDescriptor.toBuilder(any(ByteArrayMarshaller.class), any(ByteArrayMarshaller.class)))
                .will(answer((Answer2<MethodDescriptor.Builder<byte[], byte[]>, ByteArrayMarshaller, ByteArrayMarshaller>) (requestMarshaller,
                                                                                                                            responseMarshaller) -> MethodDescriptor.<byte[], byte[]>newBuilder()
                        .setRequestMarshaller(requestMarshaller)
                        .setResponseMarshaller(responseMarshaller)
                        .setType(methodType)
                        .setFullMethodName("my-service/my-method")));
    }

    @Test
    void should_return_proxy_when_UNARY_methodType() {
        // given
        givenMethod(UNARY);

        // when
        var serverMethodDefinition = proxyMethod(originalMethodDescriptor, delegateChannel);

        // then
        assertThat(serverMethodDefinition).isNotNull();
        assertThat(serverMethodDefinition.getServerCallHandler()).isNotNull();
    }

    @Test
    void should_return_proxy_when_CLIENT_STREAMING_methodType() {
        // given
        givenMethod(CLIENT_STREAMING);

        // when
        var serverMethodDefinition = proxyMethod(originalMethodDescriptor, delegateChannel);

        // then
        assertThat(serverMethodDefinition).isNotNull();
        assertThat(serverMethodDefinition.getServerCallHandler()).isNotNull();
    }

    @Test
    void should_return_proxy_when_SERVER_STREAMING_methodType() {
        // given
        givenMethod(SERVER_STREAMING);

        // when
        var serverMethodDefinition = proxyMethod(originalMethodDescriptor, delegateChannel);

        // then
        assertThat(serverMethodDefinition).isNotNull();
        assertThat(serverMethodDefinition.getServerCallHandler()).isNotNull();
    }

    @Test
    void should_return_proxy_when_BIDI_STREAMING_methodType() {
        // given
        givenMethod(BIDI_STREAMING);

        // when
        var serverMethodDefinition = proxyMethod(originalMethodDescriptor, delegateChannel);

        // then
        assertThat(serverMethodDefinition).isNotNull();
        assertThat(serverMethodDefinition.getServerCallHandler()).isNotNull();
    }

    @Test
    void should_throwException_when_UNKNOWN_methodType() {
        // given
        givenMethod(UNKNOWN);

        // when
        var caughtException = catchException(() -> proxyMethod(originalMethodDescriptor, delegateChannel));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(GrpcGatewayException.class)
                .hasMessage("my-service/my-method has unknown type")
                .hasNoCause();
    }

}
