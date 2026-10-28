package com.tradeway.reporting;

import java.math.BigDecimal;

public record ReportingSummary(
    long totalTrades,
    BigDecimal buyVolume,
    BigDecimal sellVolume,
    BigDecimal netVolume,
    double buySellRatio
) {
}
