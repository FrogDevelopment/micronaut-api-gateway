package com.frogdevelopment.micronaut.gateway.http.core.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

import org.reactivestreams.Publisher;

import com.frogdevelopment.micronaut.gateway.http.core.proxy.RequestMutator;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;
import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.filter.ServerFilterPhase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@ServerFilter("/api/**") // how to make it configurable using properties ? check ServerFilterRouteBuilder
@RequiredArgsConstructor
@SuppressWarnings("java:S1452")
public class ApiFilter implements Ordered {

    private final RequestMutator requestMutator;
    private final ProxyHttpClient proxyHttpClient;

    @RequestFilter
    public Publisher<MutableHttpResponse<?>> filterRequest(HttpRequest<?> request) {
        return requestMutator
                .mutate(request)
                .flatMapMany(target -> Flux.from(proxyHttpClient.proxy(target)))
                .switchIfEmpty(createNotFoundResponse(request))
                .onErrorResume(handleException(request));
    }

    @NonNull
    private static Mono<MutableHttpResponse<String>> createNotFoundResponse(final HttpRequest<?> request) {
        return Mono.fromSupplier(() -> {
            log.warn("Failed to find matching service for request [{}]", request.getPath());
            return HttpResponse.notFound("No service found to proxy [%s]".formatted(request.getPath()));
        });
    }

    @NonNull
    private static Function<Throwable, Mono<MutableHttpResponse<?>>> handleException(final HttpRequest<?> request) {
        return e -> {
            log.error("Failed to proxy [{}]", request.getPath(), e);
            if (e instanceof final NoAvailableServiceException noAvailableServiceException) {
                return Mono.just(HttpResponse.notFound(noAvailableServiceException.getMessage()));
            } else {
                return Mono.just(HttpResponse.serverError("An unexpected error occurred: %s".formatted(e.getMessage())));
            }
        };
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }
}
