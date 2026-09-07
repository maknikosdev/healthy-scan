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
        // JitPack hosts the Tesseract4Android OCR library used by the label scanner
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "HealthyScan"
include(":app")
