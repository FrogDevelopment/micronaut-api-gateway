package com.frogdevelopment.micronaut.gateway.grpc.marshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.frogdevelopment.micronaut.gateway.grpc.GrpcGatewayException;
import com.google.common.io.ByteStreams;

import io.grpc.MethodDescriptor;

/**
 * Prevent decoding the incoming stream by specifying a marshaller which simply passes the binary data through
 */
public final class ByteArrayMarshaller implements MethodDescriptor.Marshaller<byte[]> {

    @Override
    public InputStream stream(final byte[] value) {
        return new ByteArrayInputStream(value);
    }

    @Override
    public byte[] parse(final InputStream stream) {
        try (stream) {
            return ByteStreams.toByteArray(stream);
        } catch (final IOException e) {
            throw new GrpcGatewayException(e);
        }
    }

}
