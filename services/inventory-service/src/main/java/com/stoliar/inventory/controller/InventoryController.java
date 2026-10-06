package com.stoliar.inventory.controller;

import com.stoliar.inventory.dto.CreateInventoryRequest;
import com.stoliar.inventory.dto.InventoryResponse;
import com.stoliar.inventory.dto.StockChangeRequest;
import com.stoliar.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public InventoryResponse getInventory(
            @PathVariable UUID productId
    ) {
        return InventoryResponse.from(
                inventoryService.getInventory(productId)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InventoryResponse createInventory(
            @Valid @RequestBody CreateInventoryRequest request
    ) {
        return InventoryResponse.from(
                inventoryService.createInventory(
                        request.productId(),
                        request.stockQuantity()
                )
        );
    }

    @PostMapping("/{productId}/increase")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InventoryResponse increaseStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockChangeRequest request
    ) {
        return InventoryResponse.from(
                inventoryService.increaseStock(
                        productId,
                        request.quantity()
                )
        );
    }

    @PostMapping("/{productId}/decrease")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InventoryResponse decreaseStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockChangeRequest request
    ) {
        return InventoryResponse.from(
                inventoryService.decreaseStock(
                        productId,
                        request.quantity()
                )
        );
    }
}