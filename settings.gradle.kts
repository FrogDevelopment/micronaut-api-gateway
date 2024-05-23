plugins {
    id("io.micronaut.platform.catalog") version "4.4.0"
}

rootProject.name = "micronaut-api-gateway"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    ":micronaut-api-gateway-http",
    ":micronaut-api-gateway-grpc"
)
