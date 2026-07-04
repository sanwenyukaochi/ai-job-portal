import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorPlugin
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Style
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.7.0"
    id("com.vanniktech.dependency.graph.generator") version "0.8.0"
    id("io.github.flumennigrum.gradle.avro") version "0.1.0" apply false
}

repositories {
    mavenCentral()
}

plugins.apply(DependencyGraphGeneratorPlugin::class.java)

configure<DependencyGraphGeneratorExtension> {
    generators.create("jetbrainsLibraries") {
        include = { dependency -> dependency.moduleGroup.startsWith("org.jetbrains") }
        children = { true }
        dependencyNode = { node, _ -> node.add(Style.FILLED, Color.rgb("#AF1DF5")) }
    }
}

allprojects {
    group = "com.kittens.exploding"
    version = "0.0.1-SNAPSHOT"
    pluginManager.apply("com.diffplug.spotless")

    pluginManager.withPlugin("com.diffplug.spotless") {
        extensions.configure<SpotlessExtension> {
            encoding("UTF-8")
            java {
                target("**/*.java")
                forbidWildcardImports()
                forbidModuleImports()
                googleJavaFormat()
                    .aosp()
                    .reflowLongStrings(false)
                    .formatJavadoc(true)
                    .reorderImports(true)
                importOrder()
                removeUnusedImports()
                formatAnnotations()
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }

            kotlin {
                target("**/*.kt")
                ktlint()
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }

            kotlinGradle {
                target("**/*.gradle.kts")
                ktlint()
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }

            gherkin {
                target("**/*.feature")
                gherkinUtils()
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }

            toml {
                target("**/*.toml")
                versionCatalog()
                    .stripQuotedKeys(true)
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }

            json {
                target("**/*.json")
                gson()
                    .indentWithSpaces(4)
                    .sortByKeys()
                    .escapeHtml()
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }
        }
    }
}

subprojects {
    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_26
            targetCompatibility = JavaVersion.VERSION_26
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(26))
            }
            withSourcesJar()
        }
    }

    pluginManager.withPlugin("org.springframework.boot") {
        extensions.configure<SpringBootExtension> {
            buildInfo()
        }
    }

    pluginManager.withPlugin("io.spring.dependency-management") {
        extensions.configure<DependencyManagementExtension> {
            imports {
                mavenBom(SpringBootPlugin.BOM_COORDINATES)
            }
            dependencies {
                dependency("io.confluent:kafka-avro-serializer:${libs.versions.kafkaAvroSerializer.get()}")
                dependency("org.apache.avro:avro:${libs.versions.avro.get()}")
                dependency("org.redisson:redisson:${libs.versions.redisson.get()}")
                dependency("cn.hutool:hutool-core:${libs.versions.hutool.get()}")
                dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:${libs.versions.springdoc.get()}")
                dependency("org.postgresql:postgresql:${libs.versions.postgresql.get()}")
                dependency("io.cucumber:cucumber-java:${libs.versions.cucumber.get()}")
                dependency("io.cucumber:cucumber-junit:${libs.versions.cucumber.get()}")
                dependency("io.cucumber:cucumber-spring:${libs.versions.cucumber.get()}")
                dependency("org.testcontainers:testcontainers:${libs.versions.testcontainers.get()}")
                dependency("org.testcontainers:kafka:${libs.versions.testcontainers.get()}")
                dependency("org.testcontainers:postgresql:${libs.versions.testcontainers.get()}")
                dependency("org.testcontainers:junit-jupiter:${libs.versions.testcontainers.get()}")
                dependency("com.tngtech.archunit:archunit-junit5:${libs.versions.archunitJunit5.get()}")
            }
        }
    }
}
