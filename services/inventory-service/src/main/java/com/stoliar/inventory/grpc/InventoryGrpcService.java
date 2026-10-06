package com.stoliar.inventory.grpc;

import com.serviceorders.common.grpc.inventory.v1.CheckAvailabilityRequest;
import com.serviceorders.common.grpc.inventory.v1.CheckAvailabilityResponse;
import com.serviceorders.common.grpc.inventory.v1.InventoryItemAvailability;
import com.serviceorders.common.grpc.inventory.v1.InventoryServiceGrpc;
import com.serviceorders.common.grpc.inventory.v1.ReleaseInventoryRequest;
import com.serviceorders.common.grpc.inventory.v1.ReleaseInventoryResponse;
import com.serviceorders.common.grpc.inventory.v1.ReserveInventoryRequest;
import com.serviceorders.common.grpc.inventory.v1.ReserveInventoryResponse;
import com.stoliar.inventory.exception.InsufficientInventoryException;
import com.stoliar.inventory.exception.InventoryNotFoundException;
import com.stoliar.inventory.service.InventoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryGrpcService
        extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final InventoryService inventoryService;

    public InventoryGrpcService(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void checkAvailability(
            CheckAvailabilityRequest request,
            StreamObserver<CheckAvailabilityResponse> responseObserver
    ) {
        try {
            List<InventoryService.InventoryRequestItem> items =
                    request.getItemsList()
                            .stream()
                            .map(item ->
                                    new InventoryService
                                            .InventoryRequestItem(
                                            parseUuid(item.getProductId()),
                                            item.getQuantity()
                                    )
                            )
                            .toList();

            List<InventoryService.AvailabilityResult> results =
                    inventoryService.checkAvailability(items);

            boolean available = results.stream()
                    .allMatch(
                            InventoryService.AvailabilityResult::available
                    );

            CheckAvailabilityResponse.Builder response =
                    CheckAvailabilityResponse.newBuilder()
                            .setAvailable(available);

            results.forEach(result ->
                    response.addItems(
                            InventoryItemAvailability
                                    .newBuilder()
                                    .setProductId(
                                            result.productId().toString()
                                    )
                                    .setRequestedQuantity(
                                            result.requestedQuantity()
                                    )
                                    .setAvailableQuantity(
                                            result.availableQuantity()
                                    )
                                    .setAvailable(
                                            result.available()
                                    )
                                    .build()
                    )
            );

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();

        } catch (InventoryNotFoundException exception) {
            onError(
                    responseObserver,
                    Status.NOT_FOUND,
                    exception.getMessage()
            );
        } catch (IllegalArgumentException exception) {
            onError(
                    responseObserver,
                    Status.INVALID_ARGUMENT,
                    exception.getMessage()
            );
        } catch (Exception exception) {
            onError(
                    responseObserver,
                    Status.INTERNAL,
                    "Failed to check inventory availability"
            );
        }
    }

    @Override
    public void reserveInventory(
            ReserveInventoryRequest request,
            StreamObserver<ReserveInventoryResponse> responseObserver
    ) {
        try {
            UUID orderId = parseUuid(request.getOrderId());

            List<InventoryService.InventoryRequestItem> items =
                    request.getItemsList()
                            .stream()
                            .map(item ->
                                    new InventoryService
                                            .InventoryRequestItem(
                                            parseUuid(item.getProductId()),
                                            item.getQuantity()
                                    )
                            )
                            .toList();

            InventoryService.ReservationResult result =
                    inventoryService.reserve(
                            orderId,
                            items
                    );

            responseObserver.onNext(
                    ReserveInventoryResponse
                            .newBuilder()
                            .setReserved(result.reserved())
                            .setReason(result.reason())
                            .build()
            );

            responseObserver.onCompleted();

        } catch (InsufficientInventoryException exception) {
            responseObserver.onNext(
                    ReserveInventoryResponse
                            .newBuilder()
                            .setReserved(false)
                            .setReason(exception.getMessage())
                            .build()
            );

            responseObserver.onCompleted();

        } catch (InventoryNotFoundException exception) {
            onError(
                    responseObserver,
                    Status.NOT_FOUND,
                    exception.getMessage()
            );

        } catch (IllegalArgumentException exception) {
            onError(
                    responseObserver,
                    Status.INVALID_ARGUMENT,
                    exception.getMessage()
            );

        } catch (Exception exception) {
            onError(
                    responseObserver,
                    Status.INTERNAL,
                    "Failed to reserve inventory"
            );
        }
    }

    @Override
    public void releaseInventory(
            ReleaseInventoryRequest request,
            StreamObserver<ReleaseInventoryResponse> responseObserver
    ) {
        try {
            UUID orderId = parseUuid(request.getOrderId());

            boolean released =
                    inventoryService.release(orderId);

            responseObserver.onNext(
                    ReleaseInventoryResponse
                            .newBuilder()
                            .setReleased(released)
                            .build()
            );

            responseObserver.onCompleted();

        } catch (IllegalArgumentException exception) {
            onError(
                    responseObserver,
                    Status.INVALID_ARGUMENT,
                    exception.getMessage()
            );

        } catch (InventoryNotFoundException exception) {
            onError(
                    responseObserver,
                    Status.NOT_FOUND,
                    exception.getMessage()
            );

        } catch (Exception exception) {
            onError(
                    responseObserver,
                    Status.INTERNAL,
                    "Failed to release inventory"
            );
        }
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "UUID value must not be blank"
            );
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid UUID: " + value,
                    exception
            );
        }
    }

    private void onError(
            StreamObserver<?> observer,
            Status status,
            String description
    ) {
        observer.onError(
                status
                        .withDescription(description)
                        .asRuntimeException()
        );
    }
}