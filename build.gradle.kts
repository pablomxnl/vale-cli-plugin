plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.5.2"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
}

group = "org.ideplugins.vale-plugin"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf(/* Plugin Dependencies */))

}

dependencies {
    implementation ("org.zeroturnaround:zt-exec:1.12"){
        exclude(group="org.slf4j", module ="slf4j-api" )
    }
    implementation("com.google.code.gson:gson" ){
        version {
            strictly("2.9.1")
        }
    }
}

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.compilerArgs = listOf("-Xlint:deprecation")
    }

    init {
        version = semver.version
    }

    patchPluginXml {
        sinceBuild.set("193.5662.53")
        untilBuild.set("231.*")
        changeNotes.set("""
    <ul>
    <li>0.0.3
      <ul>
      <li>Fixed issue in chrome os where vale binary wasn't autodetected in system path</li>
      <li>Vale configuration file now optional, let the binary do it's magic to find the configuration</li>
      </ul>
    </li>    
    <li>0.0.2
      <ul>
      <li>Autodetect if Vale CLI is in system path</li>
      </ul>
    </li>
    <li>0.0.1 Initial version
      <ul>
      <li>Check current file</li>
      <li>Check all files in project</li>
      <li>Check multiple files selected in project tree (that have an extension matching the configured files)</li>
      </ul>
    </li>
    </ul>            
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("JBM_CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("JBM_PRIVATE_KEY"))
        password.set(System.getenv("JBM_PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("JBM_PUBLISH_TOKEN"))
    }
}