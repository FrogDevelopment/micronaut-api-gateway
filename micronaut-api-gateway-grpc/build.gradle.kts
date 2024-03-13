import com.google.protobuf.gradle.id

plugins {
    id("io.micronaut.minimal.library") version "4.3.3"
    id("com.google.protobuf") version "0.9.3"
}

// todo how to be sure to use the same versions than the ones defined in "micronaut-grpc-bom" (grpc.version & protobuf.version) ?
// micronaut-bom:4.2.1 -> micronaut-grpc-bom:4.0.1
// see https://central.sonatype.com/artifact/io.micronaut.platform/micronaut-platform/4.2.1
// see https://central.sonatype.com/artifact/io.micronaut.grpc/micronaut-grpc-bom/4.2.0
val protobuf = "3.25.1"
val grpc = "1.59.1"
val lombokVersion = "1.18.24"
val mockitoInline = "5.2.0"
val grpcmock = "0.10.1"

dependencies {
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    // Micronaut processor defined after Lombok
    annotationProcessor("io.micronaut:micronaut-inject-java")

    implementation("io.grpc:grpc-netty")
    implementation("io.micronaut.grpc:micronaut-grpc-client-runtime")
    implementation("io.micronaut.grpc:micronaut-grpc-server-runtime")

    // from doc https://micronaut-projects.github.io/micronaut-grpc/latest/guide/#server
    // adding health check service for gRPC: https://github.com/grpc/grpc/blob/master/doc/health-checking.md
    runtimeOnly("io.grpc:grpc-services")
    runtimeOnly("io.micronaut.discovery:micronaut-discovery-client")

    testImplementation(enforcedPlatform("io.grpc:grpc-bom:${grpc}"))
    testImplementation("io.micronaut:micronaut-context")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-inline:$mockitoInline")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.grpcmock:grpcmock-junit5:$grpcmock")
    testImplementation("io.grpc:grpc-testing")
    // do not replace with jakarta (yet), as it is still needed by protoc-gen-grpc-java
    testImplementation("javax.annotation:javax.annotation-api")

    testRuntimeOnly(enforcedPlatform("io.grpc:grpc-bom:${grpc}"))
    testRuntimeOnly("ch.qos.logback:logback-classic")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.yaml:snakeyaml")

    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("io.grpc:grpc-protobuf")
    testCompileOnly("io.grpc:grpc-stub")
    testCompileOnly("com.google.protobuf:protobuf-java")
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.frogdevelopment.micronaut.*")
    }
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
        artifact = "com.google.protobuf:protoc:${protobuf}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpc}"
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
