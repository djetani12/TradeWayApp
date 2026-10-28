package com.tradeway.dto;

import java.math.BigDecimal;

public record PositionResponse(
    String symbol,
    int quantity,
    BigDecimal averageCost
) {
}
