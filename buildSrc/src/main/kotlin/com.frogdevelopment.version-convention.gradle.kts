plugins {
    id("org.ajoberstar.grgit")
}

afterEvaluate {
    computeProjectVersion()
}

fun computeProjectVersion() {
    val branchName = grgit.branch.current().name

    println("Current branch: $branchName")

    val computedVersion = when (branchName) {
        "HEAD" -> handleHead()
        "develop" -> handleDevelop()
        else -> handleBranch(branchName)
    }

    allprojects {
        group = rootProject.group
        version = computedVersion
    }

    println("Computed version: $version")
}

fun handleHead(): String {
    val githubRefName = System.getenv("GITHUB_REF_NAME")
    if (githubRefName == null || githubRefName.isEmpty()) {
        throw GradleException("One does not simply build from HEAD. Checkout to matching local branch !!")
    }
    return githubRefName
}

fun handleDevelop(): String {
    return "develop-SNAPSHOT"
}

fun handleBranch(branchName: String): String {
    val matchBranchResult = """^(?<type>\w+)/(?<details>.+)?$""".toRegex().find(branchName)
    val branchType = matchBranchResult!!.groups["type"]?.value!!
    val branchDetails = matchBranchResult.groups["details"]?.value!!

    return "$branchType-$branchDetails-SNAPSHOT"
}
