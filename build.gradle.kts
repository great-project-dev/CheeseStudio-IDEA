plugins {
//  id("java")
////  id("org.jetbrains.kotlin.jvm") version "2.2.20"
//    id("org.jetbrains.kotlin.jvm") version "1.9.23"
//  id("org.jetbrains.intellij") version "1.17.4"
////    id("org.jetbrains.intellij.platform") version "2.9.0"
////    id("org.jetbrains.intellij.platform") version "2.9.0"
//  kotlin("plugin.serialization") version "2.0.0"
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "coco.cheesestudio"
version = "1.1.12"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IU", "2024.2.1")
        bundledPlugin("com.intellij.java")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add necessary plugin dependencies for compilation here, example:
        // bundledPlugin("com.intellij.java")
    }
  implementation("org.codehaus.jettison:jettison:1.5.4")
  implementation ("org.java-websocket:Java-WebSocket:1.5.7")
  implementation("io.ktor:ktor-server-websockets:2.3.12")
  implementation("io.ktor:ktor-server-status-pages-jvm:2.3.12")
  implementation("io.ktor:ktor-server-netty:2.3.12")
  implementation("io.ktor:ktor-server-core:2.3.12")
  implementation("org.tomlj:tomlj:1.1.1")
  implementation ("com.fifesoft:rsyntaxtextarea:3.5.1") // 使用最新版本
  //解析ymal文件
  implementation("org.yaml:snakeyaml:2.2")
  implementation("org.freemarker:freemarker:2.3.33")
  implementation(fileTree(file("libs")) {
    include("*.jar")
    include("*.aar")
  })

}
intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "232"
            untilBuild = "251.*"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
}
//// Configure Gradle IntelliJ Plugin
//// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
//intellij {
//  version.set("2024.2.1")
//  type.set("IU")
////  updateSinceUntilBuild = true
////  localPath.set("C:\\Program Files\\JetBrains\\PyCharm 2025.1.1.1")
//  plugins.set(listOf("com.intellij.java"))
////  plugins.set(listOf("PythonCore"))
//}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }
//  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions.jvmTarget = "17"
//  }

//  patchPluginXml {
//    sinceBuild.set("232")
//    untilBuild.set("251.*")
//  }

//  signPlugin {
//    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//    privateKey.set(System.getenv("PRIVATE_KEY"))
//    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
//  }

//  publishPlugin {
//    token.set(System.getenv("PUBLISH_TOKEN"))
//  }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}