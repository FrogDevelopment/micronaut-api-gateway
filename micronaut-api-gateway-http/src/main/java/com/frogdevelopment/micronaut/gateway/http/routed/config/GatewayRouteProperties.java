package com.frogdevelopment.micronaut.gateway.http.routed.config;

import lombok.Data;

import java.util.Map;
import jakarta.inject.Singleton;

import com.frogdevelopment.micronaut.gateway.http.routed.validation.annotation.UniqueRoutes;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.serde.annotation.Serdeable;

@Data
@Singleton
@Serdeable
@ConfigurationProperties("gateway")
public class GatewayRouteProperties {

    @UniqueRoutes
    private Map<String, GatewayRoute> routes;

}
