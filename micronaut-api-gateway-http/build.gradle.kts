plugins {
    id("com.frogdevelopment.library-convention")
    id("com.frogdevelopment.jreleaser.publish-convention")
}

dependencies {
    annotationProcessor(mn.micronaut.http.validation)
    annotationProcessor(mn.micronaut.serde.processor)
    annotationProcessor(mn.micronaut.validation.processor)

    implementation(mn.micronaut.cache.caffeine)
    implementation(mn.micronaut.http.client)
    implementation(mn.micronaut.management)
    implementation(mn.micronaut.reactor)
    implementation(mn.micronaut.reactor.http.client)
    implementation(mn.micronaut.serde.jackson)
    implementation(mn.micronaut.validation)

    runtimeOnly(mn.micronaut.discovery.client)

    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(libs.wiremock)
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            pom {
                name = "Micronaut API Gateway - HTTP"
                description = "API Gateway for HTTP calls"
            }
        }
    }
}
