import com.google.protobuf.gradle.id

plugins {
    id("com.frogdevelopment.library-convention")
    id("com.google.protobuf")
}

dependencies {
    implementation("io.grpc:grpc-netty")
    implementation(mn.micronaut.grpc.client.runtime)
    implementation(mn.micronaut.grpc.server.runtime)

    // from doc https://micronaut-projects.github.io/micronaut-grpc/latest/guide/#server
    // adding health check service for gRPC: https://github.com/grpc/grpc/blob/master/doc/health-checking.md
    runtimeOnly(mn.grpc.services)
    runtimeOnly(mn.micronaut.discovery.client)

    testImplementation(mn.micronaut.context.asProvider())
    testImplementation(libs.grpcmock.junit5)
    testImplementation("io.grpc:grpc-testing")
    // do not replace with jakarta (yet), as it is still needed by protoc-gen-grpc-java
    testImplementation("javax.annotation:javax.annotation-api")

    testCompileOnly(mn.grpc.protobuf)
    testCompileOnly(mn.grpc.stub)
    testCompileOnly("com.google.protobuf:protobuf-java")
}

sourceSets {
    test {
        java {
            srcDirs(
                "build/generated/source/proto/test/grpc",
                "build/generated/source/proto/test/java"
            )
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${mn.versions.protobuf.get()}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${mn.versions.grpc.asProvider().get()}"
        }
    }
    generateProtoTasks {
        ofSourceSet("test").configureEach {
            plugins {
                id("grpc")
            }
        }
    }
}

tasks.getByName("testClasses").dependsOn("generateTestProto")
