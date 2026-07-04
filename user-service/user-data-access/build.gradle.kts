plugins {
    java
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common:common-domain"))
    implementation(project(":user-service:user-domain:user-application-service"))
    implementation(project(":user-service:user-domain:user-domain-core"))

    implementation("org.springframework:spring-context")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
