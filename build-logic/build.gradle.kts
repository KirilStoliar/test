plugins {
    `kotlin-dsl`
}

group = "com.serviceorders.buildlogic"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {

    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.5.6")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")

}