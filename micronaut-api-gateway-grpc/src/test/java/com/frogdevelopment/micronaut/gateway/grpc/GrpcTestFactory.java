package com.frogdevelopment.micronaut.gateway.grpc;

import com.frogdevelopment.micronaut.gateway.grpc.mock.model.HelloServiceGrpc;

import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.grpc.server.GrpcServerChannel;

@Factory
class GrpcTestFactory {

    @Bean
    HelloServiceGrpc.HelloServiceBlockingStub blockingStub(@GrpcChannel(GrpcServerChannel.NAME) ManagedChannel channel) {
        return HelloServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    HelloServiceGrpc.HelloServiceStub nonBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) ManagedChannel channel) {
        return HelloServiceGrpc.newStub(channel);
    }
}
