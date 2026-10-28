package com.tradeway.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradeway.model.AccountLedgerEntry;
import com.tradeway.model.AccountStatus;
import com.tradeway.model.CustomerAccount;
import com.tradeway.model.ExecutionAudit;
import com.tradeway.model.Instrument;
import com.tradeway.model.OrderSide;
import com.tradeway.model.OrderStatus;
import com.tradeway.model.TradeFill;
import com.tradeway.model.TradeOrder;
import com.tradeway.model.TradePosition;
import com.tradeway.repository.AccountLedgerEntryRepository;
import com.tradeway.repository.AuditRepository;
import com.tradeway.repository.CustomerAccountRepository;
import com.tradeway.repository.FillRepository;
import com.tradeway.repository.InstrumentRepository;
import com.tradeway.repository.OrderRepository;
import com.tradeway.repository.PositionRepository;

@Service
public class TradingService {

    private final CustomerAccountRepository customerAccountRepository;
    private final InstrumentRepository instrumentRepository;
    private final OrderRepository orderRepository;
    private final PositionRepository positionRepository;
    private final FillRepository fillRepository;
    private final AuditRepository auditRepository;
    private final AccountLedgerEntryRepository ledgerRepository;
    private final Clock clock;

    public TradingService(CustomerAccountRepository customerAccountRepository,
                          InstrumentRepository instrumentRepository,
                          OrderRepository orderRepository,
                          PositionRepository positionRepository,
                          FillRepository fillRepository,
                          AuditRepository auditRepository,
                          AccountLedgerEntryRepository ledgerRepository,
                          Clock clock) {
        this.customerAccountRepository = customerAccountRepository;
        this.instrumentRepository = instrumentRepository;
        this.orderRepository = orderRepository;
        this.positionRepository = positionRepository;
        this.fillRepository = fillRepository;
        this.auditRepository = auditRepository;
        this.ledgerRepository = ledgerRepository;
        this.clock = clock;
    }

    @Transactional
    public TradeOrder placeOrder(String accountNumber, String symbol, OrderSide side, int quantity) {
        return submitOrder(accountNumber, symbol, side, quantity, false);
    }

    @Transactional
    public TradeOrder createPendingOrder(String accountNumber, String symbol, OrderSide side, int quantity) {
        return submitOrder(accountNumber, symbol, side, quantity, true);
    }

    @Transactional
    public TradeOrder cancelOrder(Long orderId, String accountNumber) {
        TradeOrder order = orderRepository.findByIdAndAccount_AccountNumber(orderId, accountNumber)
            .orElseThrow(() -> new IllegalArgumentException("Order not found for the requested account"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return order;
        }

        order.markCancelled("Cancelled by customer");
        orderRepository.save(order);
        auditRepository.save(new ExecutionAudit(order.getAccount(), "ORDER_CANCELLED", order.getClientOrderId(),
            "Cancelled pending order for " + order.getInstrument().getSymbol()));
        return order;
    }

    @Transactional
    public void processPendingOrders() {
        if (!isMarketOpen()) {
            return;
        }

        List<TradeOrder> pendingOrders = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
        for (TradeOrder order : pendingOrders) {
            if (order.getStatus() != OrderStatus.PENDING) {
                continue;
            }

            BigDecimal quote = getQuoteForInstrument(order.getInstrument());
            if (quote == null || quote.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            executeOrder(order, quote);
        }
    }

    private TradeOrder submitOrder(String accountNumber, String symbol, OrderSide side, int quantity, boolean forcePending) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return rejectedOrder(null, null, side, quantity, "Account number is required");
        }
        if (side == null) {
            return rejectedOrder(null, symbol, null, quantity, "Order side is required");
        }
        if (quantity <= 0) {
            return rejectedOrder(null, symbol, side, quantity, "Order quantity must be greater than zero");
        }

        CustomerAccount account = customerAccountRepository.findByAccountNumber(accountNumber)
            .orElse(null);
        Instrument instrument = instrumentRepository.findBySymbolIgnoreCase(symbol).orElse(null);

        if (account == null) {
            return rejectedOrder(null, symbol, side, quantity, "Account not found");
        }
        if (instrument == null) {
            return rejectedOrder(account, symbol, side, quantity, "Instrument not found");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            return rejectedOrder(account, symbol, side, quantity, "Account is not active for trading");
        }

        TradeOrder order = new TradeOrder(account, instrument, side, quantity, null, UUID.randomUUID().toString());
        orderRepository.save(order);

        if (forcePending) {
            order.setStatus(OrderStatus.PENDING);
            order.setReason("Queued for execution");
            return orderRepository.save(order);
        }

        if (!isMarketOpen()) {
            order.setStatus(OrderStatus.PENDING);
            order.setReason("Market closed; order queued for execution");
            return orderRepository.save(order);
        }

        BigDecimal quote = getQuoteForInstrument(instrument);
        if (quote == null || quote.compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus(OrderStatus.PENDING);
            order.setReason("Quote not available; order queued for execution");
            return orderRepository.save(order);
        }

        if (side == OrderSide.BUY) {
            BigDecimal notional = quote.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);
            if (account.getCashBalance().compareTo(notional) < 0) {
                return rejectOrder(order, "Insufficient cash for this buy order");
            }
        } else {
            int available = getPositionQuantity(account, instrument);
            if (available < quantity) {
                return rejectOrder(order, "Insufficient holdings for this sell order");
            }
        }

        return executeOrder(order, quote);
    }

    private TradeOrder rejectOrder(TradeOrder order, String reason) {
        if (order == null) {
            return null;
        }
        order.markRejected(reason);
        return orderRepository.save(order);
    }

    private TradeOrder rejectedOrder(CustomerAccount account, String symbol, OrderSide side, int quantity, String reason) {
        if (account == null || symbol == null || side == null) {
            return null;
        }

        Instrument instrument = instrumentRepository.findBySymbolIgnoreCase(symbol).orElse(null);
        if (instrument == null) {
            return null;
        }

        TradeOrder order = new TradeOrder(account, instrument, side, quantity, null, UUID.randomUUID().toString());
        order.markRejected(reason);
        return orderRepository.save(order);
    }

    @Transactional
    public TradeOrder executeOrder(TradeOrder order, BigDecimal price) {
        if (order == null || price == null) {
            return order;
        }

        CustomerAccount account = order.getAccount();
        Instrument instrument = order.getInstrument();
        BigDecimal notional = price.multiply(BigDecimal.valueOf(order.getQuantity())).setScale(4, RoundingMode.HALF_UP);

        if (order.getSide() == OrderSide.BUY) {
            account.setCashBalance(account.getCashBalance().subtract(notional));
            account.setBuyingPower(account.getBuyingPower().subtract(notional));
        } else {
            account.setCashBalance(account.getCashBalance().add(notional));
            account.setBuyingPower(account.getBuyingPower().add(notional));
        }
        customerAccountRepository.save(account);

        order.markFilled(price, order.getQuantity());
        orderRepository.save(order);

        TradePosition position = positionRepository.findByAccountAndInstrument(account, instrument)
            .orElseGet(() -> new TradePosition(account, instrument, 0, BigDecimal.ZERO.setScale(4)));

        if (order.getSide() == OrderSide.BUY) {
            int newQuantity = position.getQuantity() + order.getQuantity();
            BigDecimal holdingValue = position.getAverageCost().multiply(BigDecimal.valueOf(position.getQuantity()));
            BigDecimal latestValue = price.multiply(BigDecimal.valueOf(order.getQuantity()));
            BigDecimal nextAverageCost = holdingValue.add(latestValue)
                .divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);
            position.setQuantity(newQuantity);
            position.setAverageCost(nextAverageCost);
        } else {
            position.setQuantity(Math.max(0, position.getQuantity() - order.getQuantity()));
        }
        positionRepository.save(position);

        fillRepository.save(new TradeFill(order, account, instrument, order.getQuantity(), price));
        ledgerRepository.save(new AccountLedgerEntry(
            account,
            order.getSide() == OrderSide.BUY ? "BUY" : "SELL",
            order.getSide() == OrderSide.BUY ? notional.negate() : notional,
            "Executed " + order.getSide() + " order for " + order.getQuantity() + " shares of " + instrument.getSymbol()
        ));

        auditRepository.save(new ExecutionAudit(
            account,
            "ORDER_FILLED",
            order.getClientOrderId(),
            "Filled " + order.getQuantity() + " of " + instrument.getSymbol() + " at " + price
        ));

        return order;
    }

    public List<TradeOrder> getOrdersForAccount(String accountNumber) {
        CustomerAccount account = customerAccountRepository.findByAccountNumber(accountNumber).orElseThrow();
        return orderRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    public List<TradePosition> getPortfolio(String accountNumber) {
        CustomerAccount account = customerAccountRepository.findByAccountNumber(accountNumber).orElseThrow();
        return positionRepository.findByAccountOrderByInstrument_SymbolAsc(account);
    }

    public BigDecimal getQuote(String symbol) {
        Instrument instrument = instrumentRepository.findBySymbolIgnoreCase(symbol).orElse(null);
        if (instrument == null) {
            return null;
        }
        return getQuoteForInstrument(instrument);
    }

    private BigDecimal getQuoteForInstrument(Instrument instrument) {
        if (instrument == null) {
            return null;
        }
        BigDecimal basePrice = instrument.getPrice();
        int symbolHash = Math.abs(instrument.getSymbol().hashCode());
        BigDecimal drift = BigDecimal.valueOf((symbolHash % 9) - 4).multiply(BigDecimal.valueOf(0.20));
        return basePrice.add(drift).setScale(4, RoundingMode.HALF_UP);
    }

    private int getPositionQuantity(CustomerAccount account, Instrument instrument) {
        return positionRepository.findByAccountAndInstrument(account, instrument)
            .map(TradePosition::getQuantity)
            .orElse(0);
    }

    private boolean isMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(clock.withZone(ZoneId.of("America/New_York")));
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        LocalTime time = now.toLocalTime();
        LocalTime open = LocalTime.of(9, 30);
        LocalTime close = LocalTime.of(16, 0);
        return !time.isBefore(open) && !time.isAfter(close);
    }
}
