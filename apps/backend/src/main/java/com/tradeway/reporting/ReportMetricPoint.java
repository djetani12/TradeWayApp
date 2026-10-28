package com.tradeway.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportMetricPoint(
    LocalDate date,
    String side,
    int quantity,
    BigDecimal volume,
    int orderCount
) {
}
