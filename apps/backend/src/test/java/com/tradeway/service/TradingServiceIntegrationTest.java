package com.tradeway.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.tradeway.model.AccountStatus;
import com.tradeway.model.CustomerAccount;
import com.tradeway.model.Instrument;
import com.tradeway.model.OrderSide;
import com.tradeway.model.OrderStatus;
import com.tradeway.model.TradeOrder;
import com.tradeway.model.UserAccount;
import com.tradeway.model.UserRole;
import com.tradeway.repository.AccountLedgerEntryRepository;
import com.tradeway.repository.AuditRepository;
import com.tradeway.repository.CustomerAccountRepository;
import com.tradeway.repository.FillRepository;
import com.tradeway.repository.InstrumentRepository;
import com.tradeway.repository.OrderRepository;
import com.tradeway.repository.PositionRepository;
import com.tradeway.repository.UserAccountRepository;

@SpringBootTest
@Import(TradingServiceIntegrationTest.MarketClockTestConfig.class)
@ActiveProfiles("test")
@Transactional
class TradingServiceIntegrationTest {

    @TestConfiguration
    static class MarketClockTestConfig {

        @Bean("marketClock")
        @Primary
        Clock testMarketClock() {
            return Clock.fixed(Instant.parse("2024-01-12T13:30:00-05:00"), ZoneId.of("America/New_York"));
        }
    }

    @Autowired
    private TradingService tradingService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private AccountLedgerEntryRepository accountLedgerEntryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private FillRepository fillRepository;

    @Autowired
    private AuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        fillRepository.deleteAll();
        auditRepository.deleteAll();
        orderRepository.deleteAll();
        positionRepository.deleteAll();
        accountLedgerEntryRepository.deleteAll();
        customerAccountRepository.deleteAll();
        userAccountRepository.deleteAll();

        UserAccount user = new UserAccount(
            "trader1",
            "trader1@example.com",
            "$2a$10$QeM7SQ4kW9hULDpLxwZZA..e9r7e8XjY4y9nQq6yNXQWCNrd1cZ52",
            "Trader",
            "One",
            EnumSet.of(UserRole.CUSTOMER),
            AccountStatus.ACTIVE
        );
        user = userAccountRepository.save(user);

        CustomerAccount account = new CustomerAccount(
            "TRW-20001",
            user,
            AccountStatus.ACTIVE,
            new BigDecimal("50000.00"),
            new BigDecimal("50000.00")
        );
        customerAccountRepository.save(account);

        ensureInstrument("AAPL", "Apple Inc.", "STOCK", new BigDecimal("214.32"));
        ensureInstrument("BND", "Bond Fund", "BOND", new BigDecimal("73.28"));
    }

    private void ensureInstrument(String symbol, String name, String assetClass, BigDecimal price) {
        instrumentRepository.findBySymbolIgnoreCase(symbol).orElseGet(() ->
            instrumentRepository.save(new Instrument(symbol, name, assetClass, price, true))
        );
    }

    @Test
    void placeBuyOrderFillsImmediatelyAndUpdatesBook() {
        TradeOrder order = tradingService.placeOrder("TRW-20001", "AAPL", OrderSide.BUY, 10);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(order.getFilledQuantity()).isEqualTo(10);
        assertThat(order.getRemainingQuantity()).isZero();

        CustomerAccount updated = customerAccountRepository.findByAccountNumber("TRW-20001").orElseThrow();
        assertThat(updated.getCashBalance()).isLessThan(new BigDecimal("50000.00"));
    }

    @Test
    void sellOrderRejectsWhenPositionIsInsufficient() {
        TradeOrder order = tradingService.placeOrder("TRW-20001", "AAPL", OrderSide.SELL, 99);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(order.getReason()).contains("holdings");
    }

    @Test
    void cancelPendingOrderRemovesOpenOrder() {
        TradeOrder order = tradingService.placeOrder("TRW-20001", "AAPL", OrderSide.BUY, 9999);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);

        TradeOrder pendingOrder = tradingService.createPendingOrder("TRW-20001", "BND", OrderSide.BUY, 5);
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

        TradeOrder cancelled = tradingService.cancelOrder(pendingOrder.getId(), "TRW-20001");
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
