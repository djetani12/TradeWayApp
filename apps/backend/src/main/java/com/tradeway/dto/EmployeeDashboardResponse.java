package com.tradeway.dto;

import java.util.List;

public record EmployeeDashboardResponse(
    String title,
    String description,
    List<Kpi> kpis,
    List<ChartPoint> chart,
    List<StatusBreakdown> statusBreakdown
) {
    public record Kpi(String label, String value) {}

    public record ChartPoint(String date, Number value) {}

    public record StatusBreakdown(String status, Number count) {}
}
