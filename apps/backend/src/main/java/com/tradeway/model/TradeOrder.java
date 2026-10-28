package com.tradeway.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "trade_orders",
    uniqueConstraints = @UniqueConstraint(name = "uk_trade_orders_client_id", columnNames = "client_order_id")
)
public class TradeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_account_id", nullable = false)
    private CustomerAccount account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "filled_quantity", nullable = false)
    private int filledQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private int remainingQuantity;

    @Column(name = "limit_price", precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "average_fill_price", precision = 19, scale = 4)
    private BigDecimal averageFillPrice;

    @Column(name = "client_order_id", length = 128, unique = true)
    private String clientOrderId;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    protected TradeOrder() {
    }

    public TradeOrder(CustomerAccount account, Instrument instrument, OrderSide side, int quantity,
                      BigDecimal limitPrice, String clientOrderId) {
        this.account = account;
        this.instrument = instrument;
        this.side = side;
        this.quantity = quantity;
        this.filledQuantity = 0;
        this.remainingQuantity = quantity;
        this.limitPrice = limitPrice == null ? null : limitPrice.setScale(4);
        this.clientOrderId = clientOrderId;
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

    public OrderSide getSide() {
        return side;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getFilledQuantity() {
        return filledQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public BigDecimal getAverageFillPrice() {
        return averageFillPrice;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setFilledQuantity(int filledQuantity) {
        this.filledQuantity = filledQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public void setAverageFillPrice(BigDecimal averageFillPrice) {
        this.averageFillPrice = averageFillPrice;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void markRejected(String reason) {
        this.status = OrderStatus.REJECTED;
        this.reason = reason;
        this.remainingQuantity = quantity;
        this.updatedAt = Instant.now();
    }

    public void markFilled(BigDecimal fillPrice, int fillQuantity) {
        this.status = OrderStatus.FILLED;
        this.filledQuantity = fillQuantity;
        this.remainingQuantity = Math.max(0, quantity - fillQuantity);
        this.averageFillPrice = fillPrice;
        this.updatedAt = Instant.now();
    }

    public void markCancelled(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.reason = reason;
        this.remainingQuantity = quantity - filledQuantity;
        this.updatedAt = Instant.now();
    }
}
