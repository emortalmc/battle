plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.emortal.minestom.battle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("dev.hollowcube:polar:1.3.1")
//    implementation("com.github.EmortalMC:TNT:4ef1b53482")
    implementation("com.github.EmortalMC:MinestomPvP:6aefcba403")

    implementation("dev.emortal.minestom:core:9c52249")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")

    implementation("dev.emortal.minestom:game-sdk:f6d9300")
//    implementation("dev.emortal.api:kurushimi-sdk:5f9fde3") {
//        exclude(group = "dev.emortal.minestom", module = "game-sdk")
//    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
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

    build { dependsOn(shadowJar) }
}