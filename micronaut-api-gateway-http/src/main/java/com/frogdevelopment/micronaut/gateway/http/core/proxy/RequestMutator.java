package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Singleton;

import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
@RequiredArgsConstructor
@SuppressWarnings("java:S1452")
public class RequestMutator {

    private final RouteTargetProvider routeTargetProvider;
    private final BeanProvider<RequestCustomizer> requestCustomizerBeanProvider;

    @NonNull
    public Mono<MutableHttpRequest<?>> mutate(final HttpRequest<?> request) {
        return routeTargetProvider.findRouteTarget(request.getPath())
                .map(routeTarget -> mutate(request, routeTarget));
    }

    @NonNull
    private MutableHttpRequest<?> mutate(final HttpRequest<?> request, final RouteTarget routeTarget) {
        final var mutatedRequest = request.mutate()
                .uri(uriBuilder -> {
                    final var targetUri = routeTarget.uri();
                    uriBuilder.scheme(targetUri.getScheme())
                            .host(targetUri.getHost());

                    if (targetUri.getPort() >= 0) {
                        uriBuilder.port(targetUri.getPort());
                    }

                    if (StringUtils.isNotEmpty(targetUri.getPath())) {
                        uriBuilder.replacePath(targetUri.getPath())
                                .path(routeTarget.newEndpoint());
                    } else {
                        uriBuilder.replacePath(routeTarget.newEndpoint());
                    }
                });

        if (log.isDebugEnabled()) {
            final var newUri = mutatedRequest.getUri();
            log.debug("Proxying [{}] to {}://{}{}",
                    request.getPath(),
                    newUri.getScheme(),
                    newUri.getAuthority(),
                    newUri.getPath());
        }

        // if some custom headers are needed, for instance
        requestCustomizerBeanProvider.ifResolvable(requestCustomizer -> requestCustomizer.customize(mutatedRequest));

        return mutatedRequest;
    }

}
