package com.selfcheckout.analytics.grpc;

import com.selfcheckout.analytics.data.ScanEvent;
import com.selfcheckout.analytics.data.ScanEventRepository;
import com.selfcheckout.grpc.analytics.AnalyticsServiceGrpc;
import com.selfcheckout.grpc.analytics.LogScanRequest;
import com.selfcheckout.grpc.analytics.LogScanResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * gRPC server side of Analytics. Checkout calls {@code LogScan} best-effort on
 * each scan. Persisting the event is all this does; ranking happens on the
 * scheduled recompute. The caller treats failures as non-fatal.
 */
@Component
public class AnalyticsGrpcService extends AnalyticsServiceGrpc.AnalyticsServiceImplBase {

    private final ScanEventRepository scanEvents;

    public AnalyticsGrpcService(ScanEventRepository scanEvents) {
        this.scanEvents = scanEvents;
    }

    @Override
    @Transactional
    public void logScan(LogScanRequest request, StreamObserver<LogScanResponse> responseObserver) {
        scanEvents.save(new ScanEvent(request.getSku(), request.getName(), LocalDateTime.now()));
        responseObserver.onNext(LogScanResponse.newBuilder().setAccepted(true).build());
        responseObserver.onCompleted();
    }
}
