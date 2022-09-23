import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.text.replace

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    application
    alias(libs.plugins.org.javamodularity.moduleplugin)
    alias(libs.plugins.org.beryx.jlink)
}

group = "com.github.lure0xaos.jrpycg"
version = "1.0.0"

description = ""

val appMainClass: String = "com.github.lure0xaos.jrpycg.RPyCG"
val appModule: String = "RPyCG"
val appName: String = "RPyCG"

val os: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()
val appIconIco: String = "src/main/resources/com/github/lure0xaos/jrpycg/ui/RPyCGFrame.ico"
val appIconPng: String = "src/main/resources/com/github/lure0xaos/jrpycg/ui/RPyCGFrame.png"
val appCopyright: String = "Lure of Chaos"
val appVendor: String = "Lure of Chaos"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project.dependencies.enforcedPlatform(libs.org.jetbrains.kotlin.kotlin.bom))
    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
    testImplementation(libs.org.jetbrains.kotlin.kotlin.test)
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

tasks.compileJava {
    modularity.inferModulePath.set(true)
    sourceCompatibility = libs.versions.java.get()
    targetCompatibility = libs.versions.java.get()
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
    }
}

application {
    mainClass.set(appMainClass)
    mainModule.set(appModule)
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
    options.set(listOf("--strip-debug", "--no-header-files", "--no-man-pages"))
    launcher {
        name = appName
        mainClass.set(appMainClass)
    }
    jpackage {
        installerType = when {
            os.isWindows -> "msi"
            os.isLinux -> "rpm"
            os.isMacOsX -> "dmg"
            else -> error("unsupported OS: $os")
        }
        installerName = appName
        appVersion = project.version.toString()
        when {
            os.isWindows -> {
                icon = rootProject.file(appIconIco).path
                installerOptions = listOf(
                    "--description", rootProject.description,
                    "--copyright", appCopyright,
                    "--vendor", appVendor,
                    "--win-dir-chooser",
                    "--win-menu",
                    "--win-menu-group", rootProject.name,
                    "--win-per-user-install",
                    "--win-shortcut"
                )
            }

            os.isLinux -> {
                icon = rootProject.file(appIconPng).path
                installerOptions = listOf(
                    "--description", rootProject.description,
                    "--copyright", appCopyright,
                    "--vendor", appVendor,
                    "--linux-menu-group", rootProject.name,
                    "--linux-shortcut"
                )
            }

            os.isMacOsX -> {
                icon = rootProject.file(appIconPng).path
            }
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
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
    }
}

tasks.jpackage {
    doLast {
        println(
            "packaged in " + uri("" + jpackageData.installerOutputDir)
                .toURL().toExternalForm().replace("file:/", "file:///")
        )
    }
}
