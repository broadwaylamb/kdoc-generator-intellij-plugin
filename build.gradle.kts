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
  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "223"
    }
  }
}
