package com.frogdevelopment.micronaut.gateway.grpc;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.grpcmock.GrpcMock.bidiStreamingMethod;
import static org.grpcmock.GrpcMock.calledMethod;
import static org.grpcmock.GrpcMock.clientStreamingMethod;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.serverStreamingMethod;
import static org.grpcmock.GrpcMock.statusException;
import static org.grpcmock.GrpcMock.unaryMethod;
import static org.grpcmock.definitions.verification.CountMatcher.once;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import jakarta.inject.Inject;

import org.grpcmock.GrpcMock;
import org.grpcmock.junit5.GrpcMockExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloServiceGrpc;
import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloServiceGrpc.HelloServiceBlockingStub;
import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloServiceGrpc.HelloServiceStub;
import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloWorld.HelloRequest;
import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloWorld.HelloResponse;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.Status;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.micronaut.context.DefaultApplicationContextBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;

@Tag("integrationTest")
@ExtendWith(GrpcMockExtension.class)
@MicronautTest(startApplication = false, contextBuilder = GrpcGatewayIntegrationTest.CustomContextBuilder.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GrpcGatewayIntegrationTest implements TestPropertyProvider {

    public static class CustomContextBuilder extends DefaultApplicationContextBuilder {

        public CustomContextBuilder() {
            // we need to start the gRPC Mock Server before launching the application context to retrieve its used port
            grpcMock = GrpcMock.grpcMock()
                    .executor(Executors.newFixedThreadPool(1))
                    .build();

            grpcMock.start();
        }
    }

    private static final Key<String> KEY_CUSTOM = Key.of("x-request-id", ASCII_STRING_MARSHALLER);

    private static GrpcMock grpcMock;

    @Inject
    private HelloServiceBlockingStub blockingStub;
    @Inject
    private HelloServiceStub nonBlockingStub;

    @AfterAll
    static void afterAll() {
        if (grpcMock != null) {
            grpcMock.stop();
        }
    }

    @NonNull
    @Override
    public Map<String, String> getProperties() {
        // injecting into the context the random port used by gRPC Mock server
        return Map.of("grpcmock.port", String.valueOf(grpcMock.getPort()));
    }

    @BeforeEach
    void beforeEach() {
        grpcMock.resetAll();
    }

    @Test
    void should_proxy_UNARY() {
        // given
        var requestId = UUID.randomUUID().toString();
        var request = HelloRequest.newBuilder()
                .setGreeting("Hello")
                .build();

        var response = HelloResponse.newBuilder()
                .setReply("world")
                .build();
        grpcMock.register(unaryMethod(HelloServiceGrpc.getSayHelloMethod())
                .withRequest(request)
                .willReturn(response));

        var extraHeaders = getExtraHeaders(requestId);

        // when
        var helloServiceStub = blockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(extraHeaders));

        var output = helloServiceStub.sayHello(request);

        // then
        grpcMock.verifyThat(
                calledMethod(HelloServiceGrpc.getSayHelloMethod())
                        .withStatusOk()
                        .withHeader(KEY_CUSTOM, requestId)
                        .withRequest(request)
                        .build(),
                once());

        assertThat(output).isEqualTo(response);
    }

    @Test
    void should_proxy_CLIENT_STREAMING() {
        // given
        var requestId = UUID.randomUUID().toString();
        var request = HelloRequest.newBuilder()
                .setGreeting("Hello")
                .build();

        var response = HelloResponse.newBuilder()
                .setReply("world")
                .build();
        grpcMock.register(clientStreamingMethod(HelloServiceGrpc.getLotsOfGreetingsMethod())
                .withHeader(KEY_CUSTOM, requestId)
                .withFirstRequest(request)
                .willReturn(response(response).withFixedDelay(200))
                .nextWillReturn(statusException(Status.NOT_FOUND))); // subsequent invocations will return status exception

        var extraHeaders = getExtraHeaders(requestId);

        // when
        var helloServiceStub = nonBlockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(extraHeaders));

        var responses = asyncClientStreamingCall(helloServiceStub::lotsOfGreetings, request);

        // then
        grpcMock.verifyThat(
                calledMethod(HelloServiceGrpc.getLotsOfGreetingsMethod())
                        .withStatusOk()
                        .withHeader(KEY_CUSTOM, requestId)
                        .withNumberOfRequests(1)
                        .withFirstRequest(request)
                        .build(),
                once());

        assertThat(responses).containsExactly(response);
    }

    @Test
    void should_proxy_SERVER_STREAMING() {
        // given
        var requestId = UUID.randomUUID().toString();
        var request = HelloRequest.newBuilder()
                .setGreeting("Hello")
                .build();

        var response = HelloResponse.newBuilder()
                .setReply("world")
                .build();
        grpcMock.register(serverStreamingMethod(HelloServiceGrpc.getLotsOfRepliesMethod())
                .withHeader(KEY_CUSTOM, requestId)
                .withRequest(request)
                .willReturn(response));

        var extraHeaders = getExtraHeaders(requestId);

        // when
        var helloServiceStub = nonBlockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(extraHeaders));

        var responses = asyncStubCall(request, helloServiceStub::lotsOfReplies);

        // then
        grpcMock.verifyThat(
                calledMethod(HelloServiceGrpc.getLotsOfRepliesMethod())
                        .withStatusOk()
                        .withHeader(KEY_CUSTOM, requestId)
                        .withRequest(request)
                        .build(),
                once());

        assertThat(responses).containsExactly(response);
    }

    @Test
    void should_proxy_BIDI_STREAMING() {
        // given
        var requestId = UUID.randomUUID().toString();
        var request = HelloRequest.newBuilder()
                .setGreeting("Hello")
                .build();

        var response = HelloResponse.newBuilder()
                .setReply("world")
                .build();
        grpcMock.register(bidiStreamingMethod(HelloServiceGrpc.getBidiHelloMethod())
                .withHeader(KEY_CUSTOM, requestId)
                .withFirstRequest(request)
                .willProxyTo(responseObserver -> new StreamObserver<>() {
                    @Override
                    public void onNext(HelloRequest request) {
                        var response = HelloResponse.newBuilder()
                                .setReply("world")
                                .build();
                        responseObserver.onNext(response);
                    }

                    @Override
                    public void onError(Throwable error) {
                        // handle error
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                }));

        var extraHeaders = getExtraHeaders(requestId);

        // when
        var helloServiceStub = nonBlockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(extraHeaders));

        var responses = asyncClientStreamingCall(helloServiceStub::bidiHello, request);

        // then
        grpcMock.verifyThat(
                calledMethod(HelloServiceGrpc.getBidiHelloMethod())
                        .withStatusOk()
                        .withHeader(KEY_CUSTOM, requestId)
                        .withNumberOfRequests(1)
                        .withFirstRequest(request)
                        .build(),
                once());

        assertThat(responses).containsExactly(response);
    }

    private static Metadata getExtraHeaders(String requestId) {
        var extraHeaders = new Metadata();
        extraHeaders.put(KEY_CUSTOM, requestId);
        return extraHeaders;
    }

    @SafeVarargs
    private static <ReqT, RespT> List<RespT> asyncClientStreamingCall(Function<StreamObserver<RespT>, StreamObserver<ReqT>> callMethod,
                                                                      ReqT... requests) {
        StreamRecorder<RespT> streamRecorder = StreamRecorder.create();
        var requestObserver = callMethod.apply(streamRecorder);
        Stream.of(requests).forEach(requestObserver::onNext);
        requestObserver.onCompleted();

        try {
            streamRecorder.awaitCompletion(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("failed waiting for response");
        }

        if (Objects.nonNull(streamRecorder.getError())) {
            throw Status.fromThrowable(streamRecorder.getError()).asRuntimeException();
        }
        return streamRecorder.getValues();
    }

    private static <ReqT, RespT> List<RespT> asyncStubCall(ReqT request, BiConsumer<ReqT, StreamObserver<RespT>> callMethod) {
        StreamRecorder<RespT> streamRecorder = StreamRecorder.create();
        callMethod.accept(request, streamRecorder);

        try {
            streamRecorder.awaitCompletion(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("failed waiting for response");
        }

        if (Objects.nonNull(streamRecorder.getError())) {
            throw Status.fromThrowable(streamRecorder.getError()).asRuntimeException();
        }
        return streamRecorder.getValues();
    }
}
