pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "YouPlot"

include(":app")

// Core modules
include(":core:core-common")
include(":core:core-domain")
include(":core:core-data")
include(":core:core-ui")

// Feature modules
include(":feature:feature-route")
include(":feature:feature-plan")
include(":feature:feature-tracker")
