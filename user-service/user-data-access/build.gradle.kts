plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":user-service:user-domain:user-application-service"))
    api(project(":common:common-data-access"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.postgresql:postgresql")
}
