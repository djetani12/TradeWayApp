package com.tradeway.dto;

import java.math.BigDecimal;

import com.tradeway.model.OrderSide;
import com.tradeway.model.OrderStatus;
import com.tradeway.model.TradeOrder;

public record TradeOrderResponse(
    Long id,
    String accountNumber,
    String symbol,
    OrderSide side,
    OrderStatus status,
    int quantity,
    int filledQuantity,
    int remainingQuantity,
    BigDecimal averageFillPrice,
    String reason
) {
    public static TradeOrderResponse from(TradeOrder order) {
        return new TradeOrderResponse(
            order.getId(),
            order.getAccount().getAccountNumber(),
            order.getInstrument().getSymbol(),
            order.getSide(),
            order.getStatus(),
            order.getQuantity(),
            order.getFilledQuantity(),
            order.getRemainingQuantity(),
            order.getAverageFillPrice(),
            order.getReason()
        );
    }
}
