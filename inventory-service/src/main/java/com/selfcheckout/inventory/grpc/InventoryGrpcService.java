package com.selfcheckout.inventory.grpc;

import com.selfcheckout.grpc.inventory.BatchStockRequest;
import com.selfcheckout.grpc.inventory.BatchStockResponse;
import com.selfcheckout.grpc.inventory.InventoryServiceGrpc;
import com.selfcheckout.grpc.inventory.StockLine;
import com.selfcheckout.grpc.inventory.StockRequest;
import com.selfcheckout.grpc.inventory.StockResponse;
import com.selfcheckout.grpc.inventory.StockResult;
import com.selfcheckout.inventory.data.InventoryStockRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

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

    /**
     * Decrements a whole basket in one transaction. Rows are locked in SKU order
     * (deterministic, so concurrent completions can't deadlock), and each SKU is
     * fulfilled up to the available stock — never below zero. Reports the actual
     * fulfilled count per SKU so Checkout can drop oversold units.
     */
    @Override
    @Transactional
    public void decrementStockBatch(BatchStockRequest request,
                                    StreamObserver<BatchStockResponse> responseObserver) {
        BatchStockResponse.Builder response = BatchStockResponse.newBuilder();
        for (StockLine line : sortedBySku(request.getLinesList())) {
            Integer current = stock.lockAndReadStock(line.getSku());
            int fulfilled = current == null ? 0 : Math.min(current, line.getQuantity());
            if (fulfilled > 0) {
                stock.adjustStock(line.getSku(), -fulfilled);
            }
            response.addResults(StockResult.newBuilder()
                    .setSku(line.getSku())
                    .setFulfilled(fulfilled)
                    .build());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /** Returns a whole basket's worth of units in one transaction (saga compensation). */
    @Override
    @Transactional
    public void incrementStockBatch(BatchStockRequest request,
                                    StreamObserver<BatchStockResponse> responseObserver) {
        BatchStockResponse.Builder response = BatchStockResponse.newBuilder();
        for (StockLine line : sortedBySku(request.getLinesList())) {
            if (line.getQuantity() > 0) {
                stock.adjustStock(line.getSku(), line.getQuantity());
            }
            response.addResults(StockResult.newBuilder()
                    .setSku(line.getSku())
                    .setFulfilled(line.getQuantity())
                    .build());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private static List<StockLine> sortedBySku(List<StockLine> lines) {
        return lines.stream().sorted(Comparator.comparing(StockLine::getSku)).toList();
    }
}
