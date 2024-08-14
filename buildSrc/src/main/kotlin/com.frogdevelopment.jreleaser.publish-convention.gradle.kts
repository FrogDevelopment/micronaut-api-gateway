plugins {
    java
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                description = project.description
                url = "https://github.com/FrogDevelopment/micronaut-api-gateway/wiki"
                inceptionYear = "2024"
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/FrogDevelopment/micronaut-api-gateway/issues"
                }
                developers {
                    developer {
                        id = "FrogDevelopper"
                        name = "Le Gall Benoît"
                        email = "legall.benoit@gmail.com"
                        url = "https://github.com/FrogDevelopper"
                        timezone = "Europe/Paris"
                    }
                }
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/FrogDevelopment/micronaut-api-gateway.git"
                    developerConnection = "scm:git:ssh://github.com:FrogDevelopment/micronaut-api-gateway.git"
                    url = "https://github.com/FrogDevelopment/micronaut-api-gateway/tree/master"
                }
                distributionManagement {
                    downloadUrl = "https://github.com/FrogDevelopment/micronaut-api-gateway/releases"
                }
            }
        }
    }

    repositories {
        maven {
            name = "jreleaser"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}
