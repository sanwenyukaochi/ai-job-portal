plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.postgresql:postgresql")
}
