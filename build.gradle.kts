plugins {
    kotlin("jvm") version "1.9.22"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        maven("https://papermc.io/repo/repository/maven-public/")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        implementation("io.github.monun:tap-api:4.9.8")
    }
}
