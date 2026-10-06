package com.stoliar.inventory.exception;

import java.util.UUID;

public class InventoryAlreadyExistsException extends RuntimeException {

    public InventoryAlreadyExistsException(UUID productId) {
        super("Inventory already exists for product: " + productId);
    }
}