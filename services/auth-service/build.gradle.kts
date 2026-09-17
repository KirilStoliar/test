plugins {
    id("service-orders.microservice")
}

dependencies {
    implementation(project(":common:common-core"))
    implementation(project(":common:common-events"))
    implementation(project(":common:common-security"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("org.liquibase:liquibase-core")

    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.confluent:kafka-avro-serializer:7.7.1")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)

    testImplementation("com.icegreen:greenmail:2.1.13")
}