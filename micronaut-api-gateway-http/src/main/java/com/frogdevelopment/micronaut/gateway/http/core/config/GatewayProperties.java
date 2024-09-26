package com.frogdevelopment.micronaut.gateway.http.core.config;

import lombok.Data;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.serde.annotation.Serdeable;

@Data
@Serdeable
@ConfigurationProperties("gateway")
public class GatewayProperties {

    boolean useHostDockerInternal = false;
}
