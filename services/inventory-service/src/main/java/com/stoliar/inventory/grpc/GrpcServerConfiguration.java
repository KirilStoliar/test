package com.stoliar.inventory.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfiguration {

    @Bean
    public Server grpcServer(
            InventoryGrpcService inventoryGrpcService,
            @Value("${grpc.server.port:9090}") int port
    ) {
        return NettyServerBuilder
                .forPort(port)
                .addService(inventoryGrpcService)
                .build();
    }
}