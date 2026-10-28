package com.tradeway.dto;

import java.math.BigDecimal;

import com.tradeway.model.CustomerAccount;

public record CustomerAccountResponse(
    Long id,
    String accountNumber,
    String status,
    BigDecimal cashBalance,
    BigDecimal buyingPower
) {

    public static CustomerAccountResponse from(CustomerAccount account) {
        return new CustomerAccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getStatus().name(),
            account.getCashBalance(),
            account.getBuyingPower()
        );
    }
}
