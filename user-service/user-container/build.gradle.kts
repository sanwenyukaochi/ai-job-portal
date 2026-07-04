plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":user-service:user-application"))
    implementation(project(":user-service:user-domain:user-application-service"))
    implementation(project(":user-service:user-domain:user-domain-core"))
    implementation(project(":user-service:user-data-access"))
    implementation(project(":user-service:user-security"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
