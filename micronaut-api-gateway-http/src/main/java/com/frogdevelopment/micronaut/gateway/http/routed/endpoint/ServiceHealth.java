package com.frogdevelopment.micronaut.gateway.http.routed.endpoint;

import java.util.List;

import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewayRoute;
import com.frogdevelopment.micronaut.gateway.http.routed.config.GatewaySubRoute;

import io.micronaut.health.HealthStatus;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
public record ServiceHealth(String uri,
                            String serviceId,
                            HealthStatus healthStatus,
                            List<GatewaySubRoute> mapping) {

    public boolean isHealthy() {
        return healthStatus == HealthStatus.UP;
    }

    static ServiceHealth fromService(final GatewayRoute gatewayRoute, final HealthStatus healthStatus) {
        return new ServiceHealth(null, gatewayRoute.getServiceId(), healthStatus, gatewayRoute.getMapping());
    }

    static ServiceHealth fromUri(final GatewayRoute gatewayRoute, final HealthStatus healthStatus) {
        return new ServiceHealth(gatewayRoute.getUri(), null, healthStatus, gatewayRoute.getMapping());
    }
}
