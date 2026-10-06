package com.stoliar.inventory.dto;

import com.stoliar.inventory.entity.InventoryItem;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID productId,
        int stockQuantity,
        int reservedQuantity,
        int availableQuantity,
        Instant createdAt,
        Instant updatedAt
) {

    public static InventoryResponse from(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getProductId(),
                item.getStockQuantity(),
                item.getReservedQuantity(),
                item.getAvailableQuantity(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}