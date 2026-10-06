plugins {
    id("service-orders.microservice")
}

dependencies {
    implementation(project(":common:common-core"))
    implementation(project(":common:common-grpc"))
    implementation(project(":common:common-security"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    implementation(libs.liquibase.core)
    implementation(libs.postgresql)

    implementation(libs.springdoc.openapi)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
}