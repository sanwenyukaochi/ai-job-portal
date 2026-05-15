plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":user-service:user-domain:user-domain-core"))
    api(project(":common:common-domain"))
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework:spring-tx")
    api("org.springframework.boot:spring-boot-starter-json")
}
