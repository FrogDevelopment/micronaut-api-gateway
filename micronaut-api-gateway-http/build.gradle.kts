plugins {
    id("com.frogdevelopment.library-convention")
}

dependencies {
    annotationProcessor(mn.micronaut.http.validation)
    annotationProcessor(mn.micronaut.serde.processor)
    annotationProcessor(mn.micronaut.validation.processor)

    implementation(mn.micronaut.cache.caffeine)
    implementation(mn.micronaut.http.client.asProvider())
    implementation(mn.micronaut.management)
    implementation(mn.micronaut.reactor.asProvider())
    implementation(mn.micronaut.reactor.http.client)
    implementation(mn.micronaut.serde.jackson)
    implementation(mn.micronaut.validation.asProvider())

    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(libs.wiremock)
}
