package com.tradeway.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tradeway.dto.CustomerAccountResponse;
import com.tradeway.dto.LedgerEntryResponse;
import com.tradeway.model.CustomerAccount;
import com.tradeway.model.UserAccount;
import com.tradeway.repository.AccountLedgerEntryRepository;
import com.tradeway.repository.CustomerAccountRepository;
import com.tradeway.repository.UserAccountRepository;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api")
public class CustomerAccountController {

    private final UserAccountRepository userAccountRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final AccountLedgerEntryRepository accountLedgerEntryRepository;

    public CustomerAccountController(UserAccountRepository userAccountRepository,
                                    CustomerAccountRepository customerAccountRepository,
                                    AccountLedgerEntryRepository accountLedgerEntryRepository) {
        this.userAccountRepository = userAccountRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.accountLedgerEntryRepository = accountLedgerEntryRepository;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<CustomerAccountResponse>> getAccounts(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(authentication.getName())
            .or(() -> userAccountRepository.findByEmailIgnoreCase(authentication.getName()))
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));

        List<CustomerAccountResponse> accounts = customerAccountRepository.findByUserAccount(user).stream()
            .map(CustomerAccountResponse::from)
            .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{accountNumber}/ledger")
    public ResponseEntity<List<LedgerEntryResponse>> getLedger(@PathVariable String accountNumber, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(authentication.getName())
            .or(() -> userAccountRepository.findByEmailIgnoreCase(authentication.getName()))
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));

        CustomerAccount account = customerAccountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Account not found"));

        if (!account.getUserAccount().getId().equals(user.getId())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(
            accountLedgerEntryRepository.findByAccountOrderByOccurredAtDesc(account).stream()
                .map(LedgerEntryResponse::from)
                .toList()
        );
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminSummary() {
        return ResponseEntity.ok("TradeWay system summary available to administrators.");
    }
}
