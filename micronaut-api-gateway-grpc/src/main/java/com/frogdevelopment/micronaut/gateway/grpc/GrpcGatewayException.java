package com.frogdevelopment.micronaut.gateway.grpc;

public class GrpcGatewayException extends RuntimeException {

    public GrpcGatewayException(final String message) {
        super(message);
    }

    public GrpcGatewayException(final Exception e) {
        super(e);
    }
}
