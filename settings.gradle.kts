rootProject.name = "LetsDoFoodBalance"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        from(files("libraries.toml"))
    }
}
