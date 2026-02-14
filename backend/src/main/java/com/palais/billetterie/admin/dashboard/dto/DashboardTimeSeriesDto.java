package com.palais.billetterie.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class DashboardTimeSeriesDto {
    private Instant day;
    private double revenueTotal;
    private long ticketsUsedCount;
    private long ordersPaidCount;
}
