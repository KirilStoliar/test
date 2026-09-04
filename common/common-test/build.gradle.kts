plugins {
    id("service-orders.java-library")
    alias(libs.plugins.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.6")
    }
}

dependencies {
    api(libs.spring.boot.starter.test)
    api(libs.spring.security.test)
    api(libs.spring.kafka.test)

    api(libs.testcontainers)
    api(libs.testcontainers.junit.jupiter)
    api(libs.testcontainers.postgresql)
    api(libs.testcontainers.mongodb)
    api(libs.testcontainers.kafka)
}