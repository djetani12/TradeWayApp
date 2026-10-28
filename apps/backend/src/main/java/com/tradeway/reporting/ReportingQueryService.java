package com.tradeway.reporting;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportingQueryService {

    private final JdbcTemplate reportingJdbcTemplate;

    public ReportingQueryService(JdbcTemplate reportingJdbcTemplate) {
        this.reportingJdbcTemplate = reportingJdbcTemplate;
    }

    public List<Map<String, Object>> getDailyActivity() {
        return reportingJdbcTemplate.queryForList(
            "SELECT metric_date AS day, metric_name AS metric, metric_value AS total_value FROM reporting_metrics WHERE metric_name IN ('BUY', 'SELL') ORDER BY metric_date DESC LIMIT 30"
        );
    }
}
