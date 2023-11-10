package com.frogdevelopment.micronaut.gateway.http.core.cache;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceEndpointCacheLoaderTest {

    private static final String INPUT = "any";

    @InjectMocks
    private ServiceEndpointCacheLoader serviceEndpointCacheLoader;

    @Mock
    private List<PathMatcher> pathMatchers;

    @Test
    void should_return_emptyOptional_when_emptyMatchers() {
        // given
        given(pathMatchers.stream()).willReturn(Stream.empty());

        // when
        var optional = serviceEndpointCacheLoader.load(INPUT);

        // then
        Assertions.assertThat(optional).isNotPresent();
    }

    @Test
    void should_return_emptyOptional_when_noneMatching() {
        // given
        var matcher_1 = mock(PathMatcher.class);
        given(matcher_1.matches(INPUT)).willReturn(null);
        var matcher_2 = mock(PathMatcher.class);
        given(matcher_2.matches(INPUT)).willReturn(null);
        var matcher_3 = mock(PathMatcher.class);
        given(matcher_3.matches(INPUT)).willReturn(null);
        given(pathMatchers.stream()).willReturn(Stream.of(matcher_1, matcher_2, matcher_3));

        // when
        var optional = serviceEndpointCacheLoader.load(INPUT);

        // then
        Assertions.assertThat(optional).isNotPresent();
    }

    @Test
    void should_return_firstMatchingOptional_when_present() {
        // given
        var matcher_1 = mock(PathMatcher.class);
        given(matcher_1.matches(INPUT)).willReturn(null);
        var matcher_2 = mock(PathMatcher.class);
        given(matcher_2.matches(INPUT)).willReturn(mock(MatchingServiceEndpoint.class));
        var matcher_3 = mock(PathMatcher.class);
        given(pathMatchers.stream()).willReturn(Stream.of(matcher_1, matcher_2, matcher_3));

        // when
        var optional = serviceEndpointCacheLoader.load(INPUT);

        // then
        Assertions.assertThat(optional).isPresent();
        then(matcher_3).shouldHaveNoInteractions();
    }

}
