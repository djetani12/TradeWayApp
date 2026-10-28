package com.tradeway.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trade_fills")
public class TradeFill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_order_id", nullable = false)
    private TradeOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_account_id", nullable = false)
    private CustomerAccount account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "execution_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal executionPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TradeFill() {
    }

    public TradeFill(TradeOrder order, CustomerAccount account, Instrument instrument, int quantity, BigDecimal executionPrice) {
        this.order = order;
        this.account = account;
        this.instrument = instrument;
        this.quantity = quantity;
        this.executionPrice = executionPrice == null ? BigDecimal.ZERO.setScale(4) : executionPrice.setScale(4);
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TradeOrder getOrder() {
        return order;
    }

    public CustomerAccount getAccount() {
        return account;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getExecutionPrice() {
        return executionPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
