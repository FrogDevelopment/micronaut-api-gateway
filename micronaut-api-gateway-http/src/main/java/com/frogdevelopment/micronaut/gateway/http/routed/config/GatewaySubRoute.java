package com.frogdevelopment.micronaut.gateway.http.routed.config;

import lombok.Builder;
import lombok.Data;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@Serdeable
public final class GatewaySubRoute {

    @Pattern(regexp = "^$|^/[a-z0-9-_/]+$", message = "context must start with a leading slash")
    private String context;

    @Nullable
    @NotBlank
    private String route;
}
