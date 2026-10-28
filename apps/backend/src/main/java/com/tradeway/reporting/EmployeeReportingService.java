package com.tradeway.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class EmployeeReportingService {

    public ReportingSummary calculateSummary(List<ReportMetricPoint> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return new ReportingSummary(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
        }

        BigDecimal buyVolume = metrics.stream()
            .filter(point -> "BUY".equalsIgnoreCase(point.side()))
            .map(ReportMetricPoint::volume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sellVolume = metrics.stream()
            .filter(point -> "SELL".equalsIgnoreCase(point.side()))
            .map(ReportMetricPoint::volume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTrades = metrics.size();
        BigDecimal netVolume = buyVolume.subtract(sellVolume);

        double ratio = sellVolume.compareTo(BigDecimal.ZERO) == 0
            ? (buyVolume.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : Double.MAX_VALUE)
            : buyVolume.divide(sellVolume, 4, RoundingMode.HALF_UP).doubleValue();

        return new ReportingSummary(totalTrades, buyVolume, sellVolume, netVolume, ratio);
    }
}
