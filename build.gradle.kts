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
extra["mockitoVersion"] = "5.12.0"
extra["springKafkaVersion"] = "4.0.5"
extra["kafkaAvroSerializerVersion"] = "7.7.0"
extra["avroVersion"] = "1.11.4"

allprojects {
    group = rootProject.group
    version = rootProject.version
}

subprojects {
    pluginManager.withPlugin("java") {
        apply(plugin = "com.diffplug.spotless")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_26
            targetCompatibility = JavaVersion.VERSION_26
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(26))
            }
            withSourcesJar()
        }

        dependencies {
            add("compileOnly", "org.projectlombok:lombok")
            add("annotationProcessor", "org.projectlombok:lombok")
            add("testCompileOnly", "org.projectlombok:lombok")
            add("testAnnotationProcessor", "org.projectlombok:lombok")
            add("implementation", "org.springframework.boot:spring-boot-starter-logging")
            add("testImplementation", "org.mockito:mockito-core")
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        tasks.named("compileJava") {
            dependsOn(tasks.named("spotlessCheck"))
        }
    }

    pluginManager.withPlugin("io.spring.dependency-management") {
        extensions.configure<DependencyManagementExtension> {
            imports {
                mavenBom(SpringBootPlugin.BOM_COORDINATES)
            }
            dependencies {
                dependency("org.springframework.kafka:spring-kafka:${property("springKafkaVersion")}")
                dependency("io.confluent:kafka-avro-serializer:${property("kafkaAvroSerializerVersion")}")
                dependency("org.apache.avro:avro:${property("avroVersion")}")
            }
        }
    }

    pluginManager.withPlugin("org.springframework.boot") {
        extensions.configure<SpringBootExtension> {
            buildInfo()
        }
    }

    pluginManager.withPlugin("com.diffplug.spotless") {
        extensions.configure<SpotlessExtension> {
            encoding("UTF-8")

            kotlin {
                ktlint()
            }

            kotlinGradle {
                ktlint()
            }
        }

        pluginManager.withPlugin("java") {
            extensions.configure<SpotlessExtension> {
                java {
                    palantirJavaFormat()
                    importOrder()
                    removeUnusedImports()
                    formatAnnotations()
                    trimTrailingWhitespace()
                    endWithNewline()
                    toggleOffOn()
                }
            }
        }
    }
}
