package com.frogdevelopment.micronaut.gateway.http.core.proxy;

import java.net.URI;

record RouteTarget(URI uri, String newEndpoint) {
}
