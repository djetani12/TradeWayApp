package com.tradeway.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tradeway.reporting.ReportMetricPoint;
import com.tradeway.reporting.ReportingSummary;
import com.tradeway.reporting.EmployeeReportingService;

class EmployeeReportingServiceTest {

    private final EmployeeReportingService service = new EmployeeReportingService();

    @Test
    void buildSummaryAggregatesBuySellAndVolumeMetrics() {
        var points = List.of(
            new ReportMetricPoint(LocalDate.of(2026, 1, 1), "BUY", 120, new BigDecimal("12000.00"), 1),
            new ReportMetricPoint(LocalDate.of(2026, 1, 1), "SELL", 80, new BigDecimal("8200.00"), 1),
            new ReportMetricPoint(LocalDate.of(2026, 1, 2), "BUY", 210, new BigDecimal("25500.00"), 2),
            new ReportMetricPoint(LocalDate.of(2026, 1, 2), "SELL", 60, new BigDecimal("6500.00"), 1)
        );

        ReportingSummary summary = service.calculateSummary(points);

        assertThat(summary.totalTrades()).isEqualTo(4);
        assertThat(summary.buyVolume()).isEqualTo(new BigDecimal("37500.00"));
        assertThat(summary.sellVolume()).isEqualTo(new BigDecimal("14700.00"));
        assertThat(summary.netVolume()).isEqualTo(new BigDecimal("22800.00"));
        assertThat(summary.buySellRatio()).isGreaterThan(1.0);
    }
}
