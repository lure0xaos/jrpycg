import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    application
    id("org.javamodularity.moduleplugin") version libs.versions.org.javamodularity.moduleplugin
    id("org.beryx.jlink") version libs.versions.org.beryx.jlink
}

group = "com.github.lure0xaos.jrpycg"
version = "1.0.0"

description = ""

val appMainClass: String = "com.github.lure0xaos.jrpycg.RPyCG"
val appModule: String = "RPyCG"
val appName: String = "RPyCG"

val os: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()
val appInstallerType: String = "msi"
val appIconIco: String = "src/main/resources/com/github/lure0xaos/jrpycg/ui/RPyCGFrame.ico"
val appIconPng: String = "src/main/resources/com/github/lure0xaos/jrpycg/ui/RPyCGFrame.png"
val appCopyright: String = "Lure of Chaos"
val appVendor: String = "Lure of Chaos"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("bom", libs.versions.kotlin.get()))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))

tasks.compileJava {
    modularity.inferModulePath.set(true)
    sourceCompatibility = libs.versions.java.get()
    targetCompatibility = libs.versions.java.get()
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = libs.versions.java.get()
}

application {
    mainClass.set(appMainClass)
    mainModule.set(appModule)
}

tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = libs.versions.java.get()
    }
}

tasks.processResources {
    filesMatching(listOf("**/*.properties")) {
        filter { line ->
            val transform: (MatchResult) -> CharSequence = { result ->
                result.groups[1]?.value?.let { key ->
                    when {
                        key == "project.name" -> appName
                        key == "project.version" -> project.version.toString()
                        key == "project.description" -> project.description
                        key == "timestamp" -> DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").format(Instant.now())
                        project.extra.has(key) -> project.extra.get(key)?.toString()
                        else -> null
                    }
                } ?: result.groups[0]?.value.toString()
            }
            line.replace(Regex("\\$\\{([^}]+)\\}"), transform).replace(Regex("@([^@]+)@"), transform)
        }
    }
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = appName
        mainClass.set(appMainClass)
    }
    jpackage {
        installerType = appInstallerType
        installerName = appName
        appVersion = project.version.toString()
        if (os.isWindows) {
            icon = rootProject.file(appIconIco).path
            installerOptions = listOf(
                "--description", rootProject.description,
                "--copyright", appCopyright,
                "--vendor", appVendor,
                "--win-dir-chooser",
                "--win-menu",
                "--win-per-user-install",
                "--win-shortcut"
            )
        }
        if (os.isLinux) {
            icon = rootProject.file(appIconPng).path
            installerOptions = listOf(
                "--description", rootProject.description,
                "--copyright", appCopyright,
                "--vendor", appVendor,
                "--linux-shortcut"
            )
        }
        if (os.isMacOsX) {
            icon = rootProject.file(appIconPng).path
        }
        installerOptions = listOf("--verbose")
    }
}

tasks.build {
//    dependsOn += tasks.jpackage
}

tasks.test {
    useJUnitPlatform()
    extensions.configure(org.javamodularity.moduleplugin.extensions.TestModuleOptions::class) {
        runOnClasspath = true
    }
}
tasks.compileTestJava {
    modularity.inferModulePath.set(true)
    extensions.configure(org.javamodularity.moduleplugin.extensions.CompileTestModuleOptions::class) {
        isCompileOnClasspath = true
    }
}
tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = libs.versions.java.get()
}
