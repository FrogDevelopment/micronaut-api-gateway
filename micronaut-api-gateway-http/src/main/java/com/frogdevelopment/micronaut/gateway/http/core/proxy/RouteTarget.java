package com.frogdevelopment.micronaut.gateway.http.core.proxy;

record RouteTarget(String serviceId, String scheme, String host, int port, String newEndpoint) {
}
