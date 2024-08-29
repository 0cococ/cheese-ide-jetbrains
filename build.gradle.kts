plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.23"
  id("org.jetbrains.intellij") version "1.17.2"
  kotlin("plugin.serialization") version "2.0.0"
}

group = "coco.cheese.ide"
version = "0.0.2"

repositories {
  mavenCentral()
}

dependencies {
  implementation("io.ktor:ktor-server-status-pages-jvm:2.3.12")
  implementation("io.ktor:ktor-server-netty:2.3.12")
  implementation("io.ktor:ktor-server-core:2.3.12")
  implementation("org.tomlj:tomlj:1.1.1")
  //解析ymal文件
  implementation("org.yaml:snakeyaml:2.2")
  implementation("org.freemarker:freemarker:2.3.33")
  implementation(fileTree(file("libs")) {
    include("*.jar")
    include("*.aar")
  })

}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2024.1.1")
  type.set("IU")
  updateSinceUntilBuild = true
//  plugins.set(listOf("com.intellij.java"))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("242.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
