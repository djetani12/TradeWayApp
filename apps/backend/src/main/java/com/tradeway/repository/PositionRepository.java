package com.tradeway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.model.CustomerAccount;
import com.tradeway.model.Instrument;
import com.tradeway.model.TradePosition;

public interface PositionRepository extends JpaRepository<TradePosition, Long> {

    Optional<TradePosition> findByAccountAndInstrument(CustomerAccount account, Instrument instrument);

    List<TradePosition> findByAccountOrderByInstrument_SymbolAsc(CustomerAccount account);

    Optional<TradePosition> findByAccount_AccountNumberAndInstrument_SymbolIgnoreCase(String accountNumber, String symbol);
}
