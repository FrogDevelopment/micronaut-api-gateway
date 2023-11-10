package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import jakarta.inject.Singleton;
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
        if (log.isDebugEnabled()) {
            log.debug("Proxying [{}] to service '{}' as {}://{}:{}{}",
                    request.getPath(),
                    routeTarget.serviceId(),
                    routeTarget.scheme(),
                    routeTarget.host(),
                    routeTarget.port(),
                    routeTarget.newEndpoint());
        }

        final var mutateRequest = request.mutate()
                .uri(uri -> uri
                        .scheme(routeTarget.scheme())
                        .host(routeTarget.host())
                        .port(routeTarget.port())
                        .replacePath(routeTarget.newEndpoint()));

        // if some custom headers are needed, for instance
        requestCustomizerBeanProvider.ifResolvable(requestCustomizer -> requestCustomizer.customize(mutateRequest));

        return mutateRequest;
    }

}
