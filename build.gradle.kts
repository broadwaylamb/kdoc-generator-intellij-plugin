import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  kotlin("jvm") version "2.0.10"
  id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "siosio"
version = "2.0.4"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("242.21829.15")
    bundledPlugins("com.intellij.java", "org.jetbrains.kotlin")

    pluginVerifier()
    instrumentationTools()
    testFramework(TestFrameworkType.Platform)
  }

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.opentest4j:opentest4j:1.3.0")
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "223"
    }
  }
}
