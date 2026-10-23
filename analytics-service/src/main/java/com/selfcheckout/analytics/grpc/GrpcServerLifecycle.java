package com.selfcheckout.analytics.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Starts and stops a gRPC server alongside the Spring context, serving every
 * {@link BindableService} bean on the configured port.
 */
@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final int port;
    private final List<BindableService> services;
    private Server server;
    private volatile boolean running;

    public GrpcServerLifecycle(@Value("${grpc.server.port}") int port,
                               List<BindableService> services) {
        this.port = port;
        this.services = services;
    }

    @Override
    public void start() {
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        services.forEach(builder::addService);
        server = builder.build();
        try {
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start gRPC server on port " + port, e);
        }
        running = true;
        log.info("gRPC server started on port {} with {} service(s)", port, services.size());
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
