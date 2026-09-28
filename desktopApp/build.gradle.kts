import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "com.dori.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Dori"
            packageVersion = "1.0.0"
            // jdeps (suggestRuntimeModules) finds the first two. jdk.crypto.ec is
            // looked up as a JCA provider at runtime, so jdeps misses it, but
            // pairing needs it for ECDH on JDK 21.
            modules("java.instrument", "jdk.unsupported", "jdk.crypto.ec")

            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                menuGroup = "Dori"
                perUserInstall = true
                // Must never change: Windows uses it to upgrade an existing install
                // in place instead of installing a second copy.
                upgradeUuid = "8f0a3c52-6d1e-4b7a-9c2e-5f4d8a1b3e70"
            }

            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
                menuGroup = "Office"
            }
        }
    }
}
