package com.tradeway.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.tradeway.model.AccountLedgerEntry;

public record LedgerEntryResponse(
    Long id,
    String entryType,
    BigDecimal amount,
    String description,
    Instant occurredAt
) {

    public static LedgerEntryResponse from(AccountLedgerEntry entry) {
        return new LedgerEntryResponse(
            entry.getId(),
            entry.getEntryType(),
            entry.getAmount(),
            entry.getDescription(),
            entry.getOccurredAt()
        );
    }
}
