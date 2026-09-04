rootProject.name = "Service_orders"

pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    }

}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()

        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }

}

include(
    "common:common-core",
    "common:common-events",
    "common:common-grpc",
    "common:common-security",
    "common:common-test",

    "services:api-gateway",
    "services:auth-service",
    "services:user-service",
    "services:product-service",
    "services:inventory-service",
    "services:order-service",
    "services:notification-service"
)