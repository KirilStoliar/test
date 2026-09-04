plugins {
    id("service-orders.java-library")
    alias(libs.plugins.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom(
            "org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"
        )
    }
}

dependencies {
    api(libs.spring.boot.starter.security)
    api(libs.spring.boot.starter.oauth2.resource.server)
}