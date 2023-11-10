rootProject.name = "micronaut-api-gateway"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    ":micronaut-api-gateway-http",
    ":micronaut-api-gateway-grpc"
)
