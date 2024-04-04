rootProject.name = "jrpycg"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("java", "21")
            version("kotlin", "1.9.23")
            version("kotlinx-serialization-core", "1.6.3")
            library("kotlinx-serialization-core", "org.jetbrains.kotlinx", "kotlinx-serialization-core")
                .versionRef("kotlinx-serialization-core")
            version("kotlinx-serialization-json", "1.6.3")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json")
                .versionRef("kotlinx-serialization-json")
            version("kotlinx-coroutines", "1.7.3")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core")
                .versionRef("kotlinx-coroutines")
            library("kotlinx-coroutines-core-jvm", "org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm")
                .versionRef("kotlinx-coroutines")
            library("kotlinx-coroutines-core-js", "org.jetbrains.kotlinx", "kotlinx-coroutines-core-js")
                .versionRef("kotlinx-coroutines")
            version("kotlinx-datetime", "0.5.0")
            library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime")
                .versionRef("kotlinx-datetime")

            version("org.javamodularity.moduleplugin", "1.8.12")
            version("org.beryx.jlink", "3.0.1")
        }
    }
}
