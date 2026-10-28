package com.tradeway.reporting;

import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradeway.outbox.OutboxEvent;
import com.tradeway.outbox.OutboxStatus;
import com.tradeway.repository.OutboxEventRepository;

@Service
public class ReportingSyncService {

    private final OutboxEventRepository outboxEventRepository;
    private final JdbcTemplate reportingJdbcTemplate;

    public ReportingSyncService(OutboxEventRepository outboxEventRepository, JdbcTemplate reportingJdbcTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.reportingJdbcTemplate = reportingJdbcTemplate;
    }

    @Transactional
    public void publish(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent(aggregateType, aggregateId, eventType, payload);
        outboxEventRepository.save(event);
    }

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : pending) {
            reportingJdbcTemplate.update(
                "INSERT INTO reporting_metrics (metric_date, metric_name, metric_value, dimension_value) VALUES (CURRENT_DATE, ?, 0, ?) " +
                    "ON CONFLICT (metric_date, metric_name, dimension_value) DO NOTHING",
                event.getEventType(),
                event.getAggregateId()
            );

            event.setStatus(OutboxStatus.PROCESSED);
            event.setProcessedAt(Instant.now());
            outboxEventRepository.save(event);
        }
    }
}
