package com.stoliar.inventory.grpc;

import io.grpc.Server;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private final Server server;

    private volatile boolean running;

    public GrpcServerLifecycle(Server server) {
        this.server = server;
    }

    @Override
    public void start() {
        try {
            server.start();
            running = true;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to start gRPC server",
                    exception
            );
        }
    }

    @Override
    public void stop() {
        server.shutdown();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}