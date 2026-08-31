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

// Note: foojay-resolver-convention removed — it makes external SSL calls (api.foojay.io)
// that fail in environments with strict SSL policies. The JBR bundled with Android Studio
// provides the JVM toolchain without needing remote resolution.

rootProject.name = "DailyPulse"
include(":app")
