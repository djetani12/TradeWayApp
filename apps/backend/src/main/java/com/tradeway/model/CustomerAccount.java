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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "customer_accounts",
    uniqueConstraints = @UniqueConstraint(name = "uk_customer_accounts_number", columnNames = "account_number")
)
public class CustomerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 32)
    private String accountNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal cashBalance = BigDecimal.ZERO.setScale(4);

    @Column(name = "buying_power", nullable = false, precision = 19, scale = 4)
    private BigDecimal buyingPower = BigDecimal.ZERO.setScale(4);

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CustomerAccount() {
    }

    public CustomerAccount(String accountNumber, UserAccount userAccount, AccountStatus status,
                          BigDecimal cashBalance, BigDecimal buyingPower) {
        this.accountNumber = accountNumber;
        this.userAccount = userAccount;
        this.status = status == null ? AccountStatus.ACTIVE : status;
        this.cashBalance = cashBalance == null ? BigDecimal.ZERO.setScale(4) : cashBalance.setScale(4);
        this.buyingPower = buyingPower == null ? BigDecimal.ZERO.setScale(4) : buyingPower.setScale(4);
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getBuyingPower() {
        return buyingPower;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public void setBuyingPower(BigDecimal buyingPower) {
        this.buyingPower = buyingPower;
    }
}
