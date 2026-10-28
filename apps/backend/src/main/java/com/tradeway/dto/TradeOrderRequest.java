package com.tradeway.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TradeOrderRequest(
    @NotBlank(message = "Symbol is required") String symbol,
    @NotNull(message = "Side is required") String side,
    @Min(value = 1, message = "Quantity must be at least 1") int quantity
) {
}
