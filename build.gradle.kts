plugins {
    java
    id("com.frogdevelopment.version-convention")
    id("com.frogdevelopment.jreleaser.deploy-convention")
}


repositories {
    mavenCentral()
}

java {
//    sourceCompatibility = JavaVersion.toVersion("21")
//    targetCompatibility = JavaVersion.toVersion("21")
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    wrapper {
        gradleVersion = "8.7"
        distributionType = Wrapper.DistributionType.ALL
    }
}
