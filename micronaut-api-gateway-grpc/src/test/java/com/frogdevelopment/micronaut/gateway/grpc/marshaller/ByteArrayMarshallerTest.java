package com.frogdevelopment.micronaut.gateway.grpc.marshaller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.frogdevelopment.micronaut.gateway.grpc.GrpcGatewayException;

@ExtendWith(MockitoExtension.class)
class ByteArrayMarshallerTest {

    @InjectMocks
    private ByteArrayMarshaller byteArrayMarshaller;

    @Mock
    private InputStream inputStream;

    @Test
    void should_stream() throws IOException {
        // given
        var value = "something wonderful".getBytes();

        // when
        try (var stream = byteArrayMarshaller.stream(value)) {
            // then
            String result = new BufferedReader(new InputStreamReader(stream))
                    .lines()
                    .collect(Collectors.joining());
            assertThat(result).isEqualTo("something wonderful");
        }
    }

    @Test
    void should_parse() throws IOException {
        // given
        try (var stream = new ByteArrayInputStream("something wonderful".getBytes())) {
            // when
            var parsed = byteArrayMarshaller.parse(stream);

            // then
            assertThat(new String(parsed, Charset.defaultCharset())).isEqualTo("something wonderful");
        }
    }

    @Test
    void should_handleIOException_when_parsing() throws IOException {
        // given
        given(inputStream.read(any(), anyInt(), anyInt())).willThrow(IOException.class);

        // when
        var caughtException = catchException(() -> byteArrayMarshaller.parse(inputStream));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(GrpcGatewayException.class)
                .hasCauseInstanceOf(IOException.class);
    }

}
