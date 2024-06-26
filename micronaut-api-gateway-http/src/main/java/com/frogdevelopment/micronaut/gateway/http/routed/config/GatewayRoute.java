package com.frogdevelopment.micronaut.gateway.http.routed.config;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

import com.frogdevelopment.micronaut.gateway.http.routed.validation.annotation.ValidGatewayRoute;

import io.micronaut.serde.annotation.Serdeable;

@Data
@Builder
@Serdeable
@ValidGatewayRoute
public final class GatewayRoute {

    private String serviceId;

    private String uri;

    private String uriHealthEndpoint;

    @Singular("mapping")
    private List<GatewaySubRoute> mapping;
}
