plugins {
    id("io.micronaut.minimal.library")
    jacoco
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.frogdevelopment.micronaut.*")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    all {
        resolutionStrategy.cacheChangingModulesFor(10, "seconds")
    }
}

tasks {
    test {
        reports.html.required.set(false)

        useJUnitPlatform {
            includeTags("unitTest")
            includeTags("integrationTest")
        }

        finalizedBy(tasks.jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(false)
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.9".toBigDecimal()
                }
            }
        }
    }
}

dependencies {
    compileOnly(mn.lombok)
    annotationProcessor(mn.lombok)
    annotationProcessor(mn.micronaut.inject.java.asProvider())

    testImplementation(mn.assertj.core)
    testImplementation(mn.mockito.junit.jupiter)
    testImplementation(libs.mockito.inline)

    testRuntimeOnly(mn.logback.classic)
    testRuntimeOnly(mn.snakeyaml)

    testAnnotationProcessor(mn.lombok)
    testCompileOnly(mn.lombok)
}
