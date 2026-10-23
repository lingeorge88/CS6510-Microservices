package com.selfcheckout.inventory.grpc;

import com.selfcheckout.grpc.inventory.InventoryServiceGrpc;
import com.selfcheckout.grpc.inventory.StockRequest;
import com.selfcheckout.grpc.inventory.StockResponse;
import com.selfcheckout.inventory.data.InventoryStockRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * gRPC server side of Inventory. Each call runs in its own local transaction and
 * commits immediately — that is exactly what the saga wants: a decrement is a
 * durable fact the moment it returns, and compensation is a separate durable fact.
 */
@Component
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final InventoryStockRepository stock;

    public InventoryGrpcService(InventoryStockRepository stock) {
        this.stock = stock;
    }

    @Override
    @Transactional
    public void decrementStock(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        boolean ok = stock.decrementStock(request.getSku()) == 1;
        responseObserver.onNext(StockResponse.newBuilder().setOk(ok).build());
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void incrementStock(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        boolean ok = stock.incrementStock(request.getSku()) == 1;
        responseObserver.onNext(StockResponse.newBuilder().setOk(ok).build());
        responseObserver.onCompleted();
    }
}
