package com.tradeway.model;

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
@Table(name = "execution_audit")
public class ExecutionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id")
    private CustomerAccount account;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "reference_id", length = 128)
    private String referenceId;

    @Column(length = 500)
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();

    protected ExecutionAudit() {
    }

    public ExecutionAudit(CustomerAccount account, String eventType, String referenceId, String details) {
        this.account = account;
        this.eventType = eventType;
        this.referenceId = referenceId;
        this.details = details;
        this.occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public CustomerAccount getAccount() {
        return account;
    }

    public String getEventType() {
        return eventType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getDetails() {
        return details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
