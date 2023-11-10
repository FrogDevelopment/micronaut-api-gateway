@Configuration
@Requires(property = "gateway.direct", value = "true", defaultValue = "true")
@Requires(missingProperty = "gateway.routes")
package com.frogdevelopment.micronaut.gateway.http.direct;

import io.micronaut.context.annotation.Configuration;
import io.micronaut.context.annotation.Requires;
