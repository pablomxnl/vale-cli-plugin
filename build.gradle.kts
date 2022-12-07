val typeIDE: String by project

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.9.0"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
}

group = "org.ideplugins.vale-plugin"

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3")
    plugins.set(
        listOf(
            "org.asciidoctor.intellij.asciidoc:0.36.12",
            "org.intellij.plugins.markdown:213.5744.9",
            "org.jetbrains.plugins.rest:213.5744.190"
        )
    )
    updateSinceUntilBuild.set(false)
    type.set(typeIDE) // Target IDE Platform

}

dependencies {
    implementation("com.google.code.gson:gson") {
        version {
            strictly("2.9.1")
        }
    }
    implementation("org.zeroturnaround:zt-exec:1.12") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}



tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.compilerArgs = listOf("-Xlint:deprecation")
    }

    withType<Test>{
        useJUnitPlatform()
    }

    runIde {
        systemProperty("idea.auto.reload.plugins", "false")

    }

    init {
        version = semver.version
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("231.*")
        changeNotes.set(
            """
    <ul>
    <li>0.0.5
      <ul>
      <li>Reparse currently opened files so the external annotators reflects the problem view faster</li>
      <li>Added notification action to show the settings screen when can't setup Vale correctly</li>      
      </ul>
    </li>      
    <li>0.0.4
      <ul>
      <li>Report results in problem view</li>
      <li>Fixed issue when switching projects</li>
      </ul>
    </li>       
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
        """.trimIndent()
        )
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