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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
    name = "trade_positions",
    uniqueConstraints = @UniqueConstraint(name = "uk_trade_positions_account_instrument", columnNames = {"customer_account_id", "instrument_id"})
)
public class TradePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_account_id", nullable = false)
    private CustomerAccount account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "average_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal averageCost = BigDecimal.ZERO.setScale(4);

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    protected TradePosition() {
    }

    public TradePosition(CustomerAccount account, Instrument instrument, int quantity, BigDecimal averageCost) {
        this.account = account;
        this.instrument = instrument;
        this.quantity = quantity;
        this.averageCost = averageCost == null ? BigDecimal.ZERO.setScale(4) : averageCost.setScale(4);
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
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

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.updatedAt = Instant.now();
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        this.updatedAt = Instant.now();
    }
}
