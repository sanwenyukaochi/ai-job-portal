plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":user-service:user-domain:user-domain-core"))
    implementation(project(":user-service:user-domain:user-application-service"))
    implementation(project(":user-service:user-data-access"))
    implementation("org.springframework.boot:spring-boot-starter")
}
