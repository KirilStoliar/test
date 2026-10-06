package com.stoliar.inventory.entity;

import com.stoliar.inventory.exception.InsufficientInventoryException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InventoryItem() {
    }

    public InventoryItem(
            UUID productId,
            int stockQuantity
    ) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }

        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "stockQuantity must not be negative"
            );
        }

        this.id = UUID.randomUUID();
        this.productId = productId;
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = 0;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void increaseStock(int quantity) {
        validatePositiveQuantity(quantity);

        stockQuantity += quantity;
    }

    public void decreaseStock(int quantity) {
        validatePositiveQuantity(quantity);

        int available = getAvailableQuantity();

        if (available < quantity) {
            throw new InsufficientInventoryException(
                    productId,
                    quantity,
                    available
            );
        }

        stockQuantity -= quantity;
    }

    public void reserve(int quantity) {
        validatePositiveQuantity(quantity);

        int available = getAvailableQuantity();

        if (available < quantity) {
            throw new InsufficientInventoryException(
                    productId,
                    quantity,
                    available
            );
        }

        reservedQuantity += quantity;
    }

    public void release(int quantity) {
        validatePositiveQuantity(quantity);

        if (reservedQuantity < quantity) {
            throw new IllegalStateException(
                    "Cannot release more inventory than reserved for product "
                            + productId
            );
        }

        reservedQuantity -= quantity;
    }

    public int getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be greater than zero"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}