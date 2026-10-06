package com.stoliar.inventory.repository;

import com.stoliar.inventory.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    Optional<InventoryReservation> findByOrderIdAndProductId(
            UUID orderId,
            UUID productId
    );

    List<InventoryReservation> findAllByOrderId(UUID orderId);

    boolean existsByOrderIdAndProductId(
            UUID orderId,
            UUID productId
    );
}