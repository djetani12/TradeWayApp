package com.tradeway.dto;

import java.math.BigDecimal;

import com.tradeway.model.Instrument;

public record InstrumentResponse(
    Long id,
    String symbol,
    String name,
    String assetClass,
    BigDecimal price,
    boolean active
) {

    public static InstrumentResponse from(Instrument instrument, BigDecimal price) {
        return new InstrumentResponse(
            instrument.getId(),
            instrument.getSymbol(),
            instrument.getName(),
            instrument.getAssetClass(),
            price == null ? instrument.getPrice() : price,
            instrument.isActive()
        );
    }
}
