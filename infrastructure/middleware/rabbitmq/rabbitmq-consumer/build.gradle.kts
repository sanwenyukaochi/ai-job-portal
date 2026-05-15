plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":infrastructure:middleware:rabbitmq:rabbitmq-config-data"))
    implementation("org.springframework.boot:spring-boot-starter-amqp")
}
