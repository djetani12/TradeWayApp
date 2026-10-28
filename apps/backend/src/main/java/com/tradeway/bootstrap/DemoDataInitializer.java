package com.tradeway.bootstrap;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tradeway.model.AccountLedgerEntry;
import com.tradeway.model.AccountStatus;
import com.tradeway.model.CustomerAccount;
import com.tradeway.model.Instrument;
import com.tradeway.model.UserAccount;
import com.tradeway.model.UserRole;
import com.tradeway.repository.AccountLedgerEntryRepository;
import com.tradeway.repository.CustomerAccountRepository;
import com.tradeway.repository.InstrumentRepository;
import com.tradeway.repository.UserAccountRepository;

@Component
public class DemoDataInitializer {

    private final UserAccountRepository userAccountRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final AccountLedgerEntryRepository accountLedgerEntryRepository;
    private final InstrumentRepository instrumentRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserAccountRepository userAccountRepository,
                              CustomerAccountRepository customerAccountRepository,
                              AccountLedgerEntryRepository accountLedgerEntryRepository,
                              InstrumentRepository instrumentRepository,
                              PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.accountLedgerEntryRepository = accountLedgerEntryRepository;
        this.instrumentRepository = instrumentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        if (userAccountRepository.count() > 0) {
            return;
        }

        UserAccount admin = new UserAccount(
            "admin",
            "admin@tradeway.local",
            passwordEncoder.encode("admin123"),
            "Ava",
            "Admin",
            EnumSet.of(UserRole.ADMIN, UserRole.ANALYST),
            AccountStatus.ACTIVE
        );
        userAccountRepository.save(admin);

        UserAccount customer = new UserAccount(
            "demo",
            "demo@tradeway.local",
            passwordEncoder.encode("demo123"),
            "Maya",
            "Investor",
            EnumSet.of(UserRole.CUSTOMER),
            AccountStatus.ACTIVE
        );
        userAccountRepository.save(customer);

        CustomerAccount customerAccount = new CustomerAccount(
            "TRW-10001",
            customer,
            AccountStatus.ACTIVE,
            BigDecimal.valueOf(25000.00),
            BigDecimal.valueOf(25000.00)
        );
        customerAccountRepository.save(customerAccount);

        accountLedgerEntryRepository.saveAll(List.of(
            new AccountLedgerEntry(customerAccount, "CREDIT", BigDecimal.valueOf(25000.00), "Initial funding"),
            new AccountLedgerEntry(customerAccount, "CREDIT", BigDecimal.valueOf(1500.00), "Bonus credit for demo account")
        ));

        instrumentRepository.saveAll(List.of(
            new Instrument("AAPL", "Apple Inc.", "STOCK", BigDecimal.valueOf(214.32), true),
            new Instrument("MSFT", "Microsoft", "STOCK", BigDecimal.valueOf(439.50), true),
            new Instrument("NVDA", "NVIDIA", "STOCK", BigDecimal.valueOf(126.15), true),
            new Instrument("V", "Visa", "STOCK", BigDecimal.valueOf(278.40), true),
            new Instrument("BND", "Vanguard Total Bond Market ETF", "ETF", BigDecimal.valueOf(73.28), true)
        ));
    }
}
