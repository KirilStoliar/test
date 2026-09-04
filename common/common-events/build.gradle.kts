plugins {
    id("service-orders.java-library")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

dependencies {
    api(libs.avro)
}

avro {
    isCreateSetters = true
    isCreateOptionalGetters = false
    isGettersReturnOptional = false
    fieldVisibility = "PRIVATE"
    outputCharacterEncoding = "UTF-8"
}