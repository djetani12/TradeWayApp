package com.tradeway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.model.AccountLedgerEntry;
import com.tradeway.model.CustomerAccount;

public interface AccountLedgerEntryRepository extends JpaRepository<AccountLedgerEntry, Long> {

    List<AccountLedgerEntry> findByAccountOrderByOccurredAtDesc(CustomerAccount account);
}
