package com.selfcheckout.catalog.grpc;

import com.selfcheckout.catalog.data.CatalogItem;
import com.selfcheckout.catalog.data.CatalogItemRepository;
import com.selfcheckout.grpc.catalog.CatalogServiceGrpc;
import com.selfcheckout.grpc.catalog.GetItemRequest;
import com.selfcheckout.grpc.catalog.GetItemResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * gRPC server side of Catalog. Checkout calls {@code GetItem} to resolve a SKU's
 * name and price without reaching into Catalog's database. A missing SKU returns
 * {@code found = false} (the caller maps that to its own ITEM_NOT_FOUND).
 */
@Component
public class CatalogGrpcService extends CatalogServiceGrpc.CatalogServiceImplBase {

    private final CatalogItemRepository catalog;

    public CatalogGrpcService(CatalogItemRepository catalog) {
        this.catalog = catalog;
    }

    @Override
    public void getItem(GetItemRequest request, StreamObserver<GetItemResponse> responseObserver) {
        Optional<CatalogItem> item = catalog.findById(request.getSku());

        GetItemResponse.Builder response = GetItemResponse.newBuilder();
        if (item.isPresent()) {
            CatalogItem found = item.get();
            response.setFound(true)
                    .setSku(found.getSku())
                    .setName(found.getName())
                    .setPrice(found.getPrice().toPlainString());
        } else {
            response.setFound(false);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
