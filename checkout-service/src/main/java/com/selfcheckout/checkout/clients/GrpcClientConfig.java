package com.selfcheckout.checkout.clients;

import com.selfcheckout.grpc.analytics.AnalyticsServiceGrpc;
import com.selfcheckout.grpc.catalog.CatalogServiceGrpc;
import com.selfcheckout.grpc.inventory.InventoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the gRPC channels and blocking stubs Checkout uses to call its
 * collaborators. Targets come from config ({@code grpc.client.*}) and default to
 * localhost for bare-JVM runs; docker-compose points them at the service names.
 * Plaintext (no TLS) — this is an internal cluster network.
 */
@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel catalogChannel(@Value("${grpc.client.catalog}") String target) {
        return ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel inventoryChannel(@Value("${grpc.client.inventory}") String target) {
        return ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel analyticsChannel(@Value("${grpc.client.analytics}") String target) {
        return ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    }

    @Bean
    public CatalogServiceGrpc.CatalogServiceBlockingStub catalogStub(ManagedChannel catalogChannel) {
        return CatalogServiceGrpc.newBlockingStub(catalogChannel);
    }

    @Bean
    public InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub(ManagedChannel inventoryChannel) {
        return InventoryServiceGrpc.newBlockingStub(inventoryChannel);
    }

    @Bean
    public AnalyticsServiceGrpc.AnalyticsServiceBlockingStub analyticsStub(ManagedChannel analyticsChannel) {
        return AnalyticsServiceGrpc.newBlockingStub(analyticsChannel);
    }
}
