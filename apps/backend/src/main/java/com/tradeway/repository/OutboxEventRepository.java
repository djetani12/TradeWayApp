package com.tradeway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.outbox.OutboxEvent;
import com.tradeway.outbox.OutboxStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
