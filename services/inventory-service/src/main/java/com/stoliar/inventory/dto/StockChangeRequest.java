package com.stoliar.inventory.dto;

import jakarta.validation.constraints.Min;

public record StockChangeRequest(

        @Min(1)
        int quantity
) {
}