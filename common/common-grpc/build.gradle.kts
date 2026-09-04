plugins {
    id("service-orders.java-library")
    id("com.google.protobuf") version "0.9.5"
}

val grpcVersion = "1.69.0"
val protobufVersion = "4.29.3"

dependencies {
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
    api(libs.grpc.netty.shaded)
    api(libs.protobuf.java)

    compileOnly(libs.javax.annotation.api)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }

    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }

    generateProtoTasks {
        all().configureEach {
            plugins {
                create("grpc")
            }
        }
    }
}