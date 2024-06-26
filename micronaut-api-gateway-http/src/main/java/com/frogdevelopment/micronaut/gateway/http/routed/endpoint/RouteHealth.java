package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
public record RouteHealth(String name,
                          ServiceHealth serviceHealth) {
}
