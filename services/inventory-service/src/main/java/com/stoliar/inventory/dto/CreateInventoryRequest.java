package com.stoliar.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInventoryRequest(

        @NotNull
        UUID productId,

        @Min(0)
        int stockQuantity
) {
}