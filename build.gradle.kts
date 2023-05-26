import org.jsoup.Jsoup

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

val ciEnvVar: String? = System.getenv("CI")
val isInCI: Boolean = ciEnvVar?.isNotEmpty() ?: false

group = properties("pluginGroup").get()

plugins {
    id("java")
    id("jacoco")
    alias(libs.plugins.asciidoc)
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.semver)
    alias(libs.plugins.jacocolog)
}


configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}

repositories {
    mavenCentral()
    maven {
        name = "GITLAB_MAVEN"
        setUrl("https://gitlab.com/api/v4/projects/44083372/packages/maven")
        credentials(HttpHeaderCredentials::class) {
            name = if (isInCI) "Job-Token" else "Private-Token"
            value = if (isInCI) System.getenv("CI_JOB_TOKEN") else System.getenv("GITLAB_TOKEN")
        }
        authentication {
            create("header", HttpHeaderAuthentication::class)
        }
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version = properties("platformVersion")
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
    updateSinceUntilBuild = false
    type = properties("platformType") // Target IDE Platform
}

dependencies {
    implementation(libs.gson)
    implementation(libs.ztexec) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.pluginsettings)
    implementation(libs.sentrysdk){
        exclude(group = "org.slf4j")
    }
    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)

    testRuntimeOnly(libs.junitplatform)
    testRuntimeOnly(libs.junitengine)

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.compilerArgs = listOf("-Xlint:deprecation","-Xlint:unchecked")
    }

    withType<Test>{
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    runIde {
        doFirst{
            copy{
                from("${projectDir}/src/test/resources/ide/options/")
                into("${intellij.sandboxDir.get()}/config/options/")
                include("*.xml")
            }
        }
        systemProperty("idea.auto.reload.plugins", "false")
        systemProperty("idea.trust.all.projects", "true")
        systemProperty("ide.show.tips.on.startup.default.value", "false")
        args = listOf("${projectDir}/src/test/resources/multiplefiles-example/")
    }

    init {
        version = semver.version
    }

    asciidoctor {
        dependsOn(processTestResources)
        setSourceDir(baseDir)
        sources {
            include("CHANGELOG.adoc")
        }
        setOutputDir(file("build/docs"))
    }

    patchPluginXml {
        dependsOn("asciidoctor")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")
        changeNotes = provider {
            Jsoup.parse(file("build/docs/CHANGELOG.html"))
                .select("#releasenotes")[0].nextElementSibling()?.children()
                ?.toString()
        }
    }

    signPlugin {
        certificateChain = environment("JBM_CERTIFICATE_CHAIN")
        privateKey = environment("JBM_PRIVATE_KEY")
        password = environment("JBM_PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        token = environment("JBM_PUBLISH_TOKEN")
    }

    jacocoTestReport {
        reports {
            xml.required = true
        }
    }


}
