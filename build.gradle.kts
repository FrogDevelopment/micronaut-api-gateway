plugins {
    java
    id("com.frogdevelopment.version-convention")
}

group = "com.frogdevelopment.micronaut"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.wrapper {
    gradleVersion = "8.7"
    distributionType = Wrapper.DistributionType.ALL
}
