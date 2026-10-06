package com.stoliar.inventory.controller;

import com.stoliar.inventory.exception.InsufficientInventoryException;
import com.stoliar.inventory.exception.InventoryAlreadyExistsException;
import com.stoliar.inventory.exception.InventoryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class InventoryExceptionHandler {

    @ExceptionHandler(InventoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(
            InventoryNotFoundException exception
    ) {
        return Map.of(
                "error", "INVENTORY_NOT_FOUND",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(InventoryAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleAlreadyExists(
            InventoryAlreadyExistsException exception
    ) {
        return Map.of(
                "error", "INVENTORY_ALREADY_EXISTS",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleInsufficientInventory(
            InsufficientInventoryException exception
    ) {
        return Map.of(
                "error", "INSUFFICIENT_INVENTORY",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(
            IllegalArgumentException exception
    ) {
        return Map.of(
                "error", "BAD_REQUEST",
                "message", exception.getMessage()
        );
    }
}