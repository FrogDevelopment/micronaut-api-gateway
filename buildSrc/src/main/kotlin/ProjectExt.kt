import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.accessors.dm.LibrariesForMn
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

// https://github.com/gradle/gradle/issues/15383
val Project.libs get() = the<LibrariesForLibs>()
val Project.mn get() = the<LibrariesForMn>()
