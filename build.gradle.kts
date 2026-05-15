import com.diffplug.gradle.spotless.SpotlessExtension
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.dsl.SpringBootExtension

plugins {
    id("org.springframework.boot") version "4.0.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.gorylenko.gradle-git-properties") version "2.5.7" apply false
    id("com.diffplug.spotless") version "8.4.0" apply false
}

group = "com.portal.job"
version = "0.0.1-SNAPSHOT"

extra["springdocVersion"] = "3.0.2"
extra["commonsLangVersion"] = "3.20.0"
extra["jjwtVersion"] = "0.13.0"
extra["postgresqlVersion"] = "42.7.10"
extra["jspecifyVersion"] = "1.0.0"
extra["redissonVersion"] = "4.3.0"
extra["hutoolVersion"] = "5.8.41"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "checkstyle")

    group = rootProject.group
    version = rootProject.version

    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom(SpringBootPlugin.BOM_COORDINATES)
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.springframework.security:spring-security-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    pluginManager.withPlugin("com.diffplug.spotless") {
        extensions.configure<SpotlessExtension> {
            encoding("UTF-8")
            java {
                palantirJavaFormat()
                importOrder()
                removeUnusedImports()
                formatAnnotations()
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }

            kotlin {
                ktlint()
            }

            kotlinGradle {
                ktlint()
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.named("compileJava") {
        dependsOn(tasks.named("spotlessCheck"))
    }
}

allprojects () {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "com.gorylenko.gradle-git-properties")

    pluginManager.withPlugin("org.springframework.boot") {
        extensions.configure<SpringBootExtension> {
            buildInfo()
        }
    }
}
