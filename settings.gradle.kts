pluginManagement {
    repositories {
        mavenCentral()
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://jitpack.io") }
        gradlePluginPortal()
        google()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://jitpack.io") }
        google()
    }
}
rootProject.name = "job-portal-system"

include(
    ":common",
    ":common:common-application",
    ":common:common-data-access",
    ":common:common-domain",
    ":infrastructure:middleware:kafka:kafka-config-data",
    ":infrastructure:middleware:kafka:kafka-producer",
    ":infrastructure:middleware:kafka:kafka-model",
    ":infrastructure:middleware:kafka:kafka-consumer",
    ":infrastructure:middleware:rabbitmq:rabbitmq-config-data",
    ":infrastructure:middleware:rabbitmq:rabbitmq-producer",
    ":infrastructure:middleware:rabbitmq:rabbitmq-consumer"
)
