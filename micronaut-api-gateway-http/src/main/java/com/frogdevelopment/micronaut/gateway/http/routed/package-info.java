@Configuration
@Requires(property = "gateway.direct", notEquals = "true", defaultValue = "true")
@Requires(property = "gateway.routes")
package com.frogdevelopment.micronaut.gateway.http.routed;

import io.micronaut.context.annotation.Configuration;
import io.micronaut.context.annotation.Requires;
