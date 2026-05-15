plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":infrastructure:middleware:kafka:kafka-model"))
    api(project(":infrastructure:middleware:kafka:kafka-config-data"))
    api("org.springframework.kafka:spring-kafka")
    api("io.confluent:kafka-avro-serializer")
}
