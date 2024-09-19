plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.emortal.minestom.battle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("dev.emortal.minestom:game-sdk:884345c")

    implementation("dev.hollowcube:polar:1.11.3")
    implementation("com.github.emortaldev:MinestomPvP:62e90b78bd") {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }

    implementation("net.kyori:adventure-text-minimessage:4.14.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    shadowJar {
        mergeServiceFiles()

        manifest {
            attributes(
                "Main-Class" to "dev.emortal.minestom.battle.Main",
                "Multi-Release" to true
            )
        }
    }

    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    build {
        dependsOn(shadowJar)
    }
}
