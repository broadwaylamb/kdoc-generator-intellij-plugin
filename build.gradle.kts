import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.0.10"
  id("org.jetbrains.intellij") version "1.17.4"
}

group = "siosio"
version = "2.0.4"

repositories {
  mavenCentral()
}

intellij {
  version.set("2024.1")
  plugins.set(listOf("Kotlin", "java"))
  type.set("IC")
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }
  withType<KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }
  patchPluginXml {
    sinceBuild.set("202")
  }
}
