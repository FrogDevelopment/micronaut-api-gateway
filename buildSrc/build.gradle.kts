plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(files(mn.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(asDependency(libs.plugins.micronaut))
    implementation(asDependency(libs.plugins.grgit))
    implementation(asDependency(libs.plugins.jreleaser))
    implementation(asDependency(libs.plugins.google.protobuf))
}

fun asDependency(provider: Provider<PluginDependency>) = with(provider.get()) { "$pluginId:$version" }
