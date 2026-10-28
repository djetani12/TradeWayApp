package com.tradeway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.model.CustomerAccount;
import com.tradeway.model.ExecutionAudit;

public interface AuditRepository extends JpaRepository<ExecutionAudit, Long> {

    List<ExecutionAudit> findByAccountOrderByOccurredAtDesc(CustomerAccount account);
}
