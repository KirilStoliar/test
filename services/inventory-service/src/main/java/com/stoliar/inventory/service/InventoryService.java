package com.stoliar.inventory.service;

import com.stoliar.inventory.entity.InventoryItem;
import com.stoliar.inventory.entity.InventoryReservation;
import com.stoliar.inventory.exception.InsufficientInventoryException;
import com.stoliar.inventory.exception.InventoryAlreadyExistsException;
import com.stoliar.inventory.exception.InventoryNotFoundException;
import com.stoliar.inventory.repository.InventoryItemRepository;
import com.stoliar.inventory.repository.InventoryReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryService(
            InventoryItemRepository inventoryItemRepository,
            InventoryReservationRepository reservationRepository
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public InventoryItem createInventory(
            UUID productId,
            int stockQuantity
    ) {
        if (inventoryItemRepository.existsByProductId(productId)) {
            throw new InventoryAlreadyExistsException(productId);
        }

        InventoryItem item = new InventoryItem(
                productId,
                stockQuantity
        );

        return inventoryItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public InventoryItem getInventory(UUID productId) {
        return inventoryItemRepository.findByProductId(productId)
                .orElseThrow(
                        () -> new InventoryNotFoundException(productId)
                );
    }

    @Transactional
    public InventoryItem increaseStock(
            UUID productId,
            int quantity
    ) {
        InventoryItem item = getForUpdate(productId);

        item.increaseStock(quantity);

        return item;
    }

    @Transactional
    public InventoryItem decreaseStock(
            UUID productId,
            int quantity
    ) {
        InventoryItem item = getForUpdate(productId);

        item.decreaseStock(quantity);

        return item;
    }

    @Transactional(readOnly = true)
    public AvailabilityResult checkAvailability(
            UUID productId,
            int quantity
    ) {
        InventoryItem item = getInventory(productId);

        int available = item.getAvailableQuantity();

        return new AvailabilityResult(
                productId,
                quantity,
                available,
                available >= quantity
        );
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResult> checkAvailability(
            List<InventoryRequestItem> items
    ) {
        return items.stream()
                .map(item ->
                        checkAvailability(
                                item.productId(),
                                item.quantity()
                        )
                )
                .toList();
    }

    @Transactional
    public ReservationResult reserve(
            UUID orderId,
            List<InventoryRequestItem> items
    ) {
        if (orderId == null) {
            throw new IllegalArgumentException(
                    "orderId must not be null"
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "items must not be empty"
            );
        }

        /*
         * First check for an already completed reservation.
         * This makes repeated Kafka/gRPC requests for the same
         * order idempotent.
         */
        boolean alreadyReserved = items.stream()
                .allMatch(item ->
                        reservationRepository
                                .findByOrderIdAndProductId(
                                        orderId,
                                        item.productId()
                                )
                                .map(InventoryReservation::isActive)
                                .orElse(false)
                );

        if (alreadyReserved) {
            return new ReservationResult(
                    true,
                    "Inventory was already reserved"
            );
        }

        /*
         * Lock inventory rows in deterministic product-id order.
         * This reduces the possibility of deadlocks when two
         * concurrent orders reserve multiple products.
         */
        List<InventoryRequestItem> sortedItems = items.stream()
                .sorted(
                        (first, second) ->
                                first.productId()
                                        .compareTo(second.productId())
                )
                .toList();

        for (InventoryRequestItem request : sortedItems) {
            InventoryReservation existingReservation =
                    reservationRepository
                            .findByOrderIdAndProductId(
                                    orderId,
                                    request.productId()
                            )
                            .orElse(null);

            if (existingReservation != null
                    && existingReservation.isActive()) {
                continue;
            }

            InventoryItem inventory =
                    getForUpdate(request.productId());

            int available = inventory.getAvailableQuantity();

            if (available < request.quantity()) {
                throw new InsufficientInventoryException(
                        request.productId(),
                        request.quantity(),
                        available
                );
            }
        }

        for (InventoryRequestItem request : sortedItems) {
            InventoryReservation existingReservation =
                    reservationRepository
                            .findByOrderIdAndProductId(
                                    orderId,
                                    request.productId()
                            )
                            .orElse(null);

            if (existingReservation != null
                    && existingReservation.isActive()) {
                continue;
            }

            InventoryItem inventory =
                    getForUpdate(request.productId());

            inventory.reserve(request.quantity());

            if (existingReservation == null) {
                reservationRepository.save(
                        new InventoryReservation(
                                orderId,
                                request.productId(),
                                request.quantity()
                        )
                );
            } else {
                throw new IllegalStateException(
                        "Released reservation cannot be reused: "
                                + orderId
                                + "/"
                                + request.productId()
                );
            }
        }

        return new ReservationResult(
                true,
                "Inventory reserved successfully"
        );
    }

    @Transactional
    public boolean release(UUID orderId) {
        List<InventoryReservation> reservations =
                reservationRepository.findAllByOrderId(orderId);

        if (reservations.isEmpty()) {
            return false;
        }

        List<InventoryReservation> activeReservations =
                reservations.stream()
                        .filter(InventoryReservation::isActive)
                        .sorted(
                                (first, second) ->
                                        first.getProductId()
                                                .compareTo(
                                                        second.getProductId()
                                                )
                        )
                        .toList();

        for (InventoryReservation reservation : activeReservations) {
            InventoryItem inventory =
                    getForUpdate(reservation.getProductId());

            inventory.release(reservation.getQuantity());

            reservation.release();
        }

        return true;
    }

    private InventoryItem getForUpdate(UUID productId) {
        return inventoryItemRepository
                .findByProductIdForUpdate(productId)
                .orElseThrow(
                        () -> new InventoryNotFoundException(productId)
                );
    }

    public record InventoryRequestItem(
            UUID productId,
            int quantity
    ) {
        public InventoryRequestItem {
            if (productId == null) {
                throw new IllegalArgumentException(
                        "productId must not be null"
                );
            }

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "quantity must be greater than zero"
                );
            }
        }
    }

    public record AvailabilityResult(
            UUID productId,
            int requestedQuantity,
            int availableQuantity,
            boolean available
    ) {
    }

    public record ReservationResult(
            boolean reserved,
            String reason
    ) {
    }
}