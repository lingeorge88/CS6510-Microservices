package com.selfcheckout.grpc.inventory;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Inventory owns stock. Checkout drives it as the saga orchestrator at completion.
 * The BATCH calls are the hot path: a whole basket is decremented in ONE gRPC call
 * and ONE local transaction (rows locked in SKU order to avoid deadlocks), instead
 * of a round-trip per unit. Stock can never go negative even under concurrent
 * completions. The single-unit calls are kept for completeness/testing.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: inventory.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class InventoryServiceGrpc {

  private InventoryServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "selfcheckout.inventory.InventoryService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.StockRequest,
      com.selfcheckout.grpc.inventory.StockResponse> getDecrementStockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DecrementStock",
      requestType = com.selfcheckout.grpc.inventory.StockRequest.class,
      responseType = com.selfcheckout.grpc.inventory.StockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.StockRequest,
      com.selfcheckout.grpc.inventory.StockResponse> getDecrementStockMethod() {
    io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.StockRequest, com.selfcheckout.grpc.inventory.StockResponse> getDecrementStockMethod;
    if ((getDecrementStockMethod = InventoryServiceGrpc.getDecrementStockMethod) == null) {
      synchronized (InventoryServiceGrpc.class) {
        if ((getDecrementStockMethod = InventoryServiceGrpc.getDecrementStockMethod) == null) {
          InventoryServiceGrpc.getDecrementStockMethod = getDecrementStockMethod =
              io.grpc.MethodDescriptor.<com.selfcheckout.grpc.inventory.StockRequest, com.selfcheckout.grpc.inventory.StockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DecrementStock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.StockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.StockResponse.getDefaultInstance()))
              .setSchemaDescriptor(new InventoryServiceMethodDescriptorSupplier("DecrementStock"))
              .build();
        }
      }
    }
    return getDecrementStockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.StockRequest,
      com.selfcheckout.grpc.inventory.StockResponse> getIncrementStockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IncrementStock",
      requestType = com.selfcheckout.grpc.inventory.StockRequest.class,
      responseType = com.selfcheckout.grpc.inventory.StockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.StockRequest,
      com.selfcheckout.grpc.inventory.StockResponse> getIncrementStockMethod() {
    io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.StockRequest, com.selfcheckout.grpc.inventory.StockResponse> getIncrementStockMethod;
    if ((getIncrementStockMethod = InventoryServiceGrpc.getIncrementStockMethod) == null) {
      synchronized (InventoryServiceGrpc.class) {
        if ((getIncrementStockMethod = InventoryServiceGrpc.getIncrementStockMethod) == null) {
          InventoryServiceGrpc.getIncrementStockMethod = getIncrementStockMethod =
              io.grpc.MethodDescriptor.<com.selfcheckout.grpc.inventory.StockRequest, com.selfcheckout.grpc.inventory.StockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IncrementStock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.StockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.StockResponse.getDefaultInstance()))
              .setSchemaDescriptor(new InventoryServiceMethodDescriptorSupplier("IncrementStock"))
              .build();
        }
      }
    }
    return getIncrementStockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.BatchStockRequest,
      com.selfcheckout.grpc.inventory.BatchStockResponse> getDecrementStockBatchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DecrementStockBatch",
      requestType = com.selfcheckout.grpc.inventory.BatchStockRequest.class,
      responseType = com.selfcheckout.grpc.inventory.BatchStockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.BatchStockRequest,
      com.selfcheckout.grpc.inventory.BatchStockResponse> getDecrementStockBatchMethod() {
    io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.BatchStockRequest, com.selfcheckout.grpc.inventory.BatchStockResponse> getDecrementStockBatchMethod;
    if ((getDecrementStockBatchMethod = InventoryServiceGrpc.getDecrementStockBatchMethod) == null) {
      synchronized (InventoryServiceGrpc.class) {
        if ((getDecrementStockBatchMethod = InventoryServiceGrpc.getDecrementStockBatchMethod) == null) {
          InventoryServiceGrpc.getDecrementStockBatchMethod = getDecrementStockBatchMethod =
              io.grpc.MethodDescriptor.<com.selfcheckout.grpc.inventory.BatchStockRequest, com.selfcheckout.grpc.inventory.BatchStockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DecrementStockBatch"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.BatchStockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.BatchStockResponse.getDefaultInstance()))
              .setSchemaDescriptor(new InventoryServiceMethodDescriptorSupplier("DecrementStockBatch"))
              .build();
        }
      }
    }
    return getDecrementStockBatchMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.BatchStockRequest,
      com.selfcheckout.grpc.inventory.BatchStockResponse> getIncrementStockBatchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IncrementStockBatch",
      requestType = com.selfcheckout.grpc.inventory.BatchStockRequest.class,
      responseType = com.selfcheckout.grpc.inventory.BatchStockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.BatchStockRequest,
      com.selfcheckout.grpc.inventory.BatchStockResponse> getIncrementStockBatchMethod() {
    io.grpc.MethodDescriptor<com.selfcheckout.grpc.inventory.BatchStockRequest, com.selfcheckout.grpc.inventory.BatchStockResponse> getIncrementStockBatchMethod;
    if ((getIncrementStockBatchMethod = InventoryServiceGrpc.getIncrementStockBatchMethod) == null) {
      synchronized (InventoryServiceGrpc.class) {
        if ((getIncrementStockBatchMethod = InventoryServiceGrpc.getIncrementStockBatchMethod) == null) {
          InventoryServiceGrpc.getIncrementStockBatchMethod = getIncrementStockBatchMethod =
              io.grpc.MethodDescriptor.<com.selfcheckout.grpc.inventory.BatchStockRequest, com.selfcheckout.grpc.inventory.BatchStockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IncrementStockBatch"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.BatchStockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.selfcheckout.grpc.inventory.BatchStockResponse.getDefaultInstance()))
              .setSchemaDescriptor(new InventoryServiceMethodDescriptorSupplier("IncrementStockBatch"))
              .build();
        }
      }
    }
    return getIncrementStockBatchMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static InventoryServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InventoryServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InventoryServiceStub>() {
        @java.lang.Override
        public InventoryServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InventoryServiceStub(channel, callOptions);
        }
      };
    return InventoryServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static InventoryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InventoryServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InventoryServiceBlockingStub>() {
        @java.lang.Override
        public InventoryServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InventoryServiceBlockingStub(channel, callOptions);
        }
      };
    return InventoryServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static InventoryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InventoryServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InventoryServiceFutureStub>() {
        @java.lang.Override
        public InventoryServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InventoryServiceFutureStub(channel, callOptions);
        }
      };
    return InventoryServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Inventory owns stock. Checkout drives it as the saga orchestrator at completion.
   * The BATCH calls are the hot path: a whole basket is decremented in ONE gRPC call
   * and ONE local transaction (rows locked in SKU order to avoid deadlocks), instead
   * of a round-trip per unit. Stock can never go negative even under concurrent
   * completions. The single-unit calls are kept for completeness/testing.
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void decrementStock(com.selfcheckout.grpc.inventory.StockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.StockResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDecrementStockMethod(), responseObserver);
    }

    /**
     */
    default void incrementStock(com.selfcheckout.grpc.inventory.StockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.StockResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIncrementStockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Decrement up to `quantity` units per SKU; reports how many were actually
     * fulfilled (0..quantity) when stock runs short.
     * </pre>
     */
    default void decrementStockBatch(com.selfcheckout.grpc.inventory.BatchStockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.BatchStockResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDecrementStockBatchMethod(), responseObserver);
    }

    /**
     * <pre>
     * Give `quantity` units per SKU back (saga compensation after a failed commit).
     * </pre>
     */
    default void incrementStockBatch(com.selfcheckout.grpc.inventory.BatchStockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.BatchStockResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIncrementStockBatchMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service InventoryService.
   * <pre>
   * Inventory owns stock. Checkout drives it as the saga orchestrator at completion.
   * The BATCH calls are the hot path: a whole basket is decremented in ONE gRPC call
   * and ONE local transaction (rows locked in SKU order to avoid deadlocks), instead
   * of a round-trip per unit. Stock can never go negative even under concurrent
   * completions. The single-unit calls are kept for completeness/testing.
   * </pre>
   */
  public static abstract class InventoryServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return InventoryServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service InventoryService.
   * <pre>
   * Inventory owns stock. Checkout drives it as the saga orchestrator at completion.
   * The BATCH calls are the hot path: a whole basket is decremented in ONE gRPC call
   * and ONE local transaction (rows locked in SKU order to avoid deadlocks), instead
   * of a round-trip per unit. Stock can never go negative even under concurrent
   * completions. The single-unit calls are kept for completeness/testing.
   * </pre>
   */
  public static final class InventoryServiceStub
      extends io.grpc.stub.AbstractAsyncStub<InventoryServiceStub> {
    private InventoryServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InventoryServiceStub(channel, callOptions);
    }

    /**
     */
    public void decrementStock(com.selfcheckout.grpc.inventory.StockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.StockResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDecrementStockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void incrementStock(com.selfcheckout.grpc.inventory.StockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.StockResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIncrementStockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Decrement up to `quantity` units per SKU; reports how many were actually
     * fulfilled (0..quantity) when stock runs short.
     * </pre>
     */
    public void decrementStockBatch(com.selfcheckout.grpc.inventory.BatchStockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.BatchStockResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDecrementStockBatchMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Give `quantity` units per SKU back (saga compensation after a failed commit).
     * </pre>
     */
    public void incrementStockBatch(com.selfcheckout.grpc.inventory.BatchStockRequest request,
        io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.BatchStockResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIncrementStockBatchMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service InventoryService.
   * <pre>
   * Inventory owns stock. Checkout drives it as the saga orchestrator at completion.
   * The BATCH calls are the hot path: a whole basket is decremented in ONE gRPC call
   * and ONE local transaction (rows locked in SKU order to avoid deadlocks), instead
   * of a round-trip per unit. Stock can never go negative even under concurrent
   * completions. The single-unit calls are kept for completeness/testing.
   * </pre>
   */
  public static final class InventoryServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<InventoryServiceBlockingStub> {
    private InventoryServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InventoryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.selfcheckout.grpc.inventory.StockResponse decrementStock(com.selfcheckout.grpc.inventory.StockRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDecrementStockMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.selfcheckout.grpc.inventory.StockResponse incrementStock(com.selfcheckout.grpc.inventory.StockRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIncrementStockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Decrement up to `quantity` units per SKU; reports how many were actually
     * fulfilled (0..quantity) when stock runs short.
     * </pre>
     */
    public com.selfcheckout.grpc.inventory.BatchStockResponse decrementStockBatch(com.selfcheckout.grpc.inventory.BatchStockRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDecrementStockBatchMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Give `quantity` units per SKU back (saga compensation after a failed commit).
     * </pre>
     */
    public com.selfcheckout.grpc.inventory.BatchStockResponse incrementStockBatch(com.selfcheckout.grpc.inventory.BatchStockRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIncrementStockBatchMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service InventoryService.
   * <pre>
   * Inventory owns stock. Checkout drives it as the saga orchestrator at completion.
   * The BATCH calls are the hot path: a whole basket is decremented in ONE gRPC call
   * and ONE local transaction (rows locked in SKU order to avoid deadlocks), instead
   * of a round-trip per unit. Stock can never go negative even under concurrent
   * completions. The single-unit calls are kept for completeness/testing.
   * </pre>
   */
  public static final class InventoryServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<InventoryServiceFutureStub> {
    private InventoryServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InventoryServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.selfcheckout.grpc.inventory.StockResponse> decrementStock(
        com.selfcheckout.grpc.inventory.StockRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDecrementStockMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.selfcheckout.grpc.inventory.StockResponse> incrementStock(
        com.selfcheckout.grpc.inventory.StockRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIncrementStockMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Decrement up to `quantity` units per SKU; reports how many were actually
     * fulfilled (0..quantity) when stock runs short.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.selfcheckout.grpc.inventory.BatchStockResponse> decrementStockBatch(
        com.selfcheckout.grpc.inventory.BatchStockRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDecrementStockBatchMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Give `quantity` units per SKU back (saga compensation after a failed commit).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.selfcheckout.grpc.inventory.BatchStockResponse> incrementStockBatch(
        com.selfcheckout.grpc.inventory.BatchStockRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIncrementStockBatchMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_DECREMENT_STOCK = 0;
  private static final int METHODID_INCREMENT_STOCK = 1;
  private static final int METHODID_DECREMENT_STOCK_BATCH = 2;
  private static final int METHODID_INCREMENT_STOCK_BATCH = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DECREMENT_STOCK:
          serviceImpl.decrementStock((com.selfcheckout.grpc.inventory.StockRequest) request,
              (io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.StockResponse>) responseObserver);
          break;
        case METHODID_INCREMENT_STOCK:
          serviceImpl.incrementStock((com.selfcheckout.grpc.inventory.StockRequest) request,
              (io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.StockResponse>) responseObserver);
          break;
        case METHODID_DECREMENT_STOCK_BATCH:
          serviceImpl.decrementStockBatch((com.selfcheckout.grpc.inventory.BatchStockRequest) request,
              (io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.BatchStockResponse>) responseObserver);
          break;
        case METHODID_INCREMENT_STOCK_BATCH:
          serviceImpl.incrementStockBatch((com.selfcheckout.grpc.inventory.BatchStockRequest) request,
              (io.grpc.stub.StreamObserver<com.selfcheckout.grpc.inventory.BatchStockResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getDecrementStockMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.selfcheckout.grpc.inventory.StockRequest,
              com.selfcheckout.grpc.inventory.StockResponse>(
                service, METHODID_DECREMENT_STOCK)))
        .addMethod(
          getIncrementStockMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.selfcheckout.grpc.inventory.StockRequest,
              com.selfcheckout.grpc.inventory.StockResponse>(
                service, METHODID_INCREMENT_STOCK)))
        .addMethod(
          getDecrementStockBatchMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.selfcheckout.grpc.inventory.BatchStockRequest,
              com.selfcheckout.grpc.inventory.BatchStockResponse>(
                service, METHODID_DECREMENT_STOCK_BATCH)))
        .addMethod(
          getIncrementStockBatchMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.selfcheckout.grpc.inventory.BatchStockRequest,
              com.selfcheckout.grpc.inventory.BatchStockResponse>(
                service, METHODID_INCREMENT_STOCK_BATCH)))
        .build();
  }

  private static abstract class InventoryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    InventoryServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.selfcheckout.grpc.inventory.InventoryProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("InventoryService");
    }
  }

  private static final class InventoryServiceFileDescriptorSupplier
      extends InventoryServiceBaseDescriptorSupplier {
    InventoryServiceFileDescriptorSupplier() {}
  }

  private static final class InventoryServiceMethodDescriptorSupplier
      extends InventoryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    InventoryServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (InventoryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new InventoryServiceFileDescriptorSupplier())
              .addMethod(getDecrementStockMethod())
              .addMethod(getIncrementStockMethod())
              .addMethod(getDecrementStockBatchMethod())
              .addMethod(getIncrementStockBatchMethod())
              .build();
        }
      }
    }
    return result;
  }
}
