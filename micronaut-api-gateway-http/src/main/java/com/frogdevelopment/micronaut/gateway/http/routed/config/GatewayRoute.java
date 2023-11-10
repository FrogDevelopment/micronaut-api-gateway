package com.frogdevelopment.micronaut.gateway.http.routed.config;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotEmpty;

@Data
@Builder
@Serdeable
public final class GatewayRoute {

    //    @NotBlank
    private String serviceId;

    @NotEmpty
    @Singular("mapping")
    private List<GatewaySubRoute> mapping;
}
