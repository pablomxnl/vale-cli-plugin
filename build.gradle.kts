import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jsoup.Jsoup

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

group = properties("pluginGroup").get()

plugins {
    id("java")
    id("jacoco")
    alias(libs.plugins.asciidoc)
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.semver)
    alias(libs.plugins.jacocolog)
    alias(libs.plugins.kotlin)
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        bundledPlugins(properties("platformBundledPlugins").map { it.split(',') })
        plugins(properties("platformPlugins").map { it.split(',') })
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }

    /*
    sourceSets {
        create("integrationTest") {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }

    val integrationTestImplementation by configurations.getting {
        extendsFrom(configurations.testImplementation.get())
    }

    dependencies {
        integrationTestImplementation(libs.junit)
        integrationTestImplementation(libs.kodeinDi)
        integrationTestImplementation(libs.kotlinxCoroutines)
    }
    */

    implementation(libs.gson)
    implementation(libs.ztexec) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.sentrysdk){
        exclude(group = "org.slf4j")
    }
    implementation(libs.annotations)
    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)

    testRuntimeOnly(libs.junitplatform)
    testRuntimeOnly(libs.junitengine)
    testImplementation(libs.junit4)

}

// Configure Gradle IntelliJ Platform Plugin
intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        changeNotes = provider {
            Jsoup.parse(file("build/docs/CHANGELOG.html"))
                    .select("#releasenotes")[0].nextElementSibling()?.children()
                    ?.toString()
        }
    }

    signing {
        certificateChainFile = file(environment("JBM_CERTIFICATE_CHAIN"))
        privateKeyFile = file(environment("JBM_PRIVATE_KEY"))
        password = environment("JBM_PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = environment("JBM_PUBLISH_TOKEN")
        channels.set(
                listOf(if ("true" == environment("PUSH_EAP").getOrElse("false")) "eap" else "default")
        )
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

}

//val integrationTest = task<Test>("integrationTest") {
//    val integrationTestSourceSet = sourceSets.getByName("integrationTest")
//    testClassesDirs = integrationTestSourceSet.output.classesDirs
//    classpath = integrationTestSourceSet.runtimeClasspath
//    systemProperty("path.to.build.plugin", tasks.prepareSandbox.get().pluginDirectory.get().asFile)
//    useJUnitPlatform()
//    dependsOn(tasks.prepareSandbox)
//}

val runIdeForManualTests by intellijPlatformTesting.runIde.registering {
    prepareSandboxTask {
        sandboxDirectory = project.layout.buildDirectory.dir("custom-sandbox")
        sandboxSuffix = ""
    }
    task {
        doFirst {
            copy {
                from("${projectDir}/src/test/resources/ide/options/")
                into(project.layout.buildDirectory.dir("custom-sandbox/config/options"))
                include("*.xml")
            }
        }
        systemProperty("idea.auto.reload.plugins", "false")
        systemProperty("idea.trust.all.projects", "true")
        systemProperty("ide.show.tips.on.startup.default.value", "false")
        systemProperty("nosplash", "true")
        args = listOf("${projectDir}/src/test/resources/multiplefiles-example/")
    }
}

val runIdeEAP by intellijPlatformTesting.runIde.registering {
    type = IntelliJPlatformType.IntellijIdeaCommunity
    version = "252-EAP-SNAPSHOT"
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.compilerArgs = listOf("-Xlint:deprecation","-Xlint:unchecked")
    }

    withType<Test>{
        useJUnitPlatform()
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
        finalizedBy(jacocoTestReport)
    }

    init {
        version = semver.version
    }

    asciidoctor {
        setSourceDir(baseDir)
        sources {
            include("CHANGELOG.adoc")
        }
        setOutputDir(file("build/docs"))
    }

    jacocoTestReport {
        classDirectories.setFrom(instrumentCode)
        reports {
            xml.required = true
        }
    }

    patchPluginXml {
        dependsOn("asciidoctor")
	    sinceBuild = properties("pluginSinceBuild")
    }


}
