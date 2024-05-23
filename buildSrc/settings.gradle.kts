import io.micronaut.gradle.catalog.LenientVersionCatalogParser
import io.micronaut.gradle.catalog.MicronautCatalogSettingsPlugin.MN_OVERRIDE_VERSIONS_TOML_FILE
import io.micronaut.gradle.catalog.VersionCatalogTomlModel
import org.gradle.internal.management.VersionCatalogBuilderInternal
import java.util.*
import java.util.function.Consumer


val properties = Properties()
file("../gradle.properties").inputStream().use { fis -> properties.load(fis) }

val micronautVersion = properties["micronautVersion"] as String

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        // can not apply the Micronaut plugin in buildSrc
        // ticket opened => https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/985
        // so I've duplicated MicronautCatalogSettingsPlugin behavior here
        val mn = create("mn") {
            description = "Micronaut Catalog"
            from("io.micronaut.platform:micronaut-platform:$micronautVersion")

            val catalogOverrideFile = file("../gradle/$MN_OVERRIDE_VERSIONS_TOML_FILE")
            if (catalogOverrideFile.exists()) {
                val parser = LenientVersionCatalogParser()
                catalogOverrideFile.inputStream().use { fis ->
                    parser.parse(fis)
                    val model = parser.model
                    fixupMicronautCatalogWith(this, model)
                }
            }
        }

        create("libs") {
            from(files("../gradle/libs.versions.toml"))
            // add Micronaut versions into Eureca catalog
            val mnCatalogBuilder = mn as VersionCatalogBuilderInternal
            val mnCatalog = mnCatalogBuilder.build()
            mnCatalog.versionAliases.forEach { alias ->
                val version = mnCatalog.getVersion(alias).version
                version(alias) {
                    strictly(version.strictVersion)
                    require(version.requiredVersion)
                    prefer(version.preferredVersion)
                    version.rejectedVersions.forEach { reject(it) }
                }
            }
        }
    }
}

rootProject.name = "buildSrc"

fun fixupMicronautCatalogWith(catalog: VersionCatalogBuilder, model: VersionCatalogTomlModel) {
    model.versionsTable.forEach { versionModel ->
        val version = versionModel.version
        val reference = versionModel.reference
        if (reference != null) {
            catalog.version(reference) {
                val strictly = version!!.strictly
                if (strictly != null) {
                    strictly(strictly)
                } else {
                    val require = version.require
                    if (require != null) {
                        require(require)
                    }
                }
                val prefer = version.prefer
                if (prefer != null) {
                    prefer(prefer)
                }
                if (version.isRejectAll) {
                    rejectAll()
                } else {
                    val rejectedVersions = version.rejectedVersions
                    rejectedVersions?.forEach(Consumer { versions: String? ->
                        reject(
                            versions
                        )
                    })
                }
            }
        }
    }
}
